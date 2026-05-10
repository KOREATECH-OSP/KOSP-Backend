package io.swkoreatech.kosp.domain.resume.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.resume.dto.request.ResumeSaveRequest;
import io.swkoreatech.kosp.domain.resume.dto.request.ResumeSaveRequest.CertificationItem;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeResponse;
import io.swkoreatech.kosp.domain.resume.model.UserResume;
import io.swkoreatech.kosp.domain.resume.repository.UserResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이력서 서비스.
 * 이력서 저장(upsert) 및 조회를 담당한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    /** 허용 가능한 자격증 상태 값. */
    private static final Set<String> ALLOWED_CERT_STATUSES = Set.of("ACQUIRED", "EXPIRED");

    private final UserResumeRepository userResumeRepository;
    private final ObjectMapper objectMapper;

    /**
     * 내 이력서를 조회한다.
     *
     * <p>저장된 이력서가 없으면 {@code resumeData = null}로 응답한다.
     * 프론트엔드는 null 수신 시 localStorage 또는 빈 기본값으로 대체한다.</p>
     *
     * @param user 인증된 사용자
     * @return 이력서 응답 (없으면 resumeData = null)
     */
    public ResumeResponse getMyResume(User user) {
        Optional<UserResume> resumeOpt = userResumeRepository.findByUserId(user.getId());
        if (resumeOpt.isEmpty()) {
            return new ResumeResponse(user.getId(), null, null);
        }

        UserResume resume = resumeOpt.get();
        Object parsed = parseJson(resume.getResumeData());
        return new ResumeResponse(user.getId(), parsed, resume.getUpdatedAt());
    }

    /**
     * 내 이력서를 저장(upsert)한다.
     *
     * <p>기존 데이터가 있으면 update, 없으면 insert한다.
     * 자격증 상태 값 보정: ACQUIRED/EXPIRED 외 값은 ACQUIRED로 교체한다.</p>
     *
     * @param user    인증된 사용자
     * @param request 저장 요청
     * @return 저장된 이력서 응답
     */
    @Transactional
    public ResumeResponse saveMyResume(User user, ResumeSaveRequest request) {
        ResumeSaveRequest sanitized = sanitizeCertifications(request);
        String json = serializeToJson(sanitized);

        UserResume resume = userResumeRepository.findByUserId(user.getId())
            .map(existing -> {
                existing.updateResumeData(json);
                return existing;
            })
            .orElseGet(() -> UserResume.builder()
                .user(user)
                .resumeData(json)
                .build());

        UserResume saved = userResumeRepository.save(resume);
        log.info("이력서 저장 완료: userId={}", user.getId());

        Object parsed = parseJson(saved.getResumeData());
        return new ResumeResponse(user.getId(), parsed, saved.getUpdatedAt());
    }

    /**
     * 특정 사용자의 공개 이력서를 조회한다.
     *
     * <p>이력서가 없거나 {@code isPublic != true}이면 404를 던진다.
     * 비공개 이력서 내용은 절대 응답하지 않는다.</p>
     *
     * @param userId 조회 대상 사용자 ID
     * @return 공개 이력서 응답
     * @throws GlobalException 이력서 없거나 비공개인 경우 (NOT_FOUND)
     */
    public ResumeResponse getPublicResume(Long userId) {
        UserResume resume = userResumeRepository.findByUserId(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (!isPublic(resume.getResumeData())) {
            throw new GlobalException(ExceptionMessage.NOT_FOUND);
        }

        Object parsed = parseJson(resume.getResumeData());
        return new ResumeResponse(userId, parsed, resume.getUpdatedAt());
    }

    // ── private helpers ──────────────────────────────────────────────

    /**
     * 자격증 상태 값 보정.
     * ACQUIRED/EXPIRED 외의 값(PREPARING, SCHEDULED 등)은 ACQUIRED로 교체한다.
     */
    private ResumeSaveRequest sanitizeCertifications(ResumeSaveRequest request) {
        if (request.certifications() == null || request.certifications().isEmpty()) {
            return request;
        }

        List<CertificationItem> sanitized = request.certifications().stream()
            .map(cert -> {
                String status = cert.status();
                if (status == null || !ALLOWED_CERT_STATUSES.contains(status)) {
                    log.warn("자격증 상태 보정: '{}' → 'ACQUIRED'", status);
                    return new CertificationItem(cert.id(), cert.name(), cert.organization(), cert.date(), "ACQUIRED");
                }
                return cert;
            })
            .toList();

        return new ResumeSaveRequest(
            request.resumeTitle(),
            request.headline(),
            request.bio(),
            request.jobRole(),
            request.techStack(),
            request.links(),
            request.education(),
            request.career(),
            request.experience(),
            request.projects(),
            request.awards(),
            sanitized,
            request.coverLetters(),
            request.isPublic()
        );
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("이력서 직렬화 실패", e);
            throw new GlobalException(ExceptionMessage.SERVER_ERROR);
        }
    }

    private Object parseJson(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            log.error("이력서 역직렬화 실패", e);
            return null;
        }
    }

    /**
     * JSON 문자열에서 {@code isPublic} 필드를 읽어 공개 여부를 반환한다.
     * 파싱 실패 또는 필드 부재 시 {@code false}로 처리한다.
     */
    @SuppressWarnings("unchecked")
    private boolean isPublic(String json) {
        if (json == null) return false;
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            Object value = map.get("isPublic");
            return Boolean.TRUE.equals(value);
        } catch (JsonProcessingException e) {
            log.error("isPublic 파싱 실패", e);
            return false;
        }
    }
}
