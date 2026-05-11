package io.swkoreatech.kosp.domain.resume.service;

import java.util.List;
import java.util.Map;
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
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeListResponse;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeResponse;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeSummaryResponse;
import io.swkoreatech.kosp.domain.resume.model.UserResume;
import io.swkoreatech.kosp.domain.resume.repository.UserResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이력서 서비스.
 *
 * <p>기존 단일 이력서 API(getMyResume, saveMyResume, getPublicResume)를
 * "기본 이력서" 기준으로 동작하도록 유지하여 하위 호환성을 보장한다.
 * 신규 다중 이력서 API(getMyResumes, createResume, getMyResumeById,
 * updateResumeById, deleteResumeById, setDefaultResume)를 추가한다.</p>
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

    // ── 하위 호환 API (기본 이력서 기준) ──────────────────────────────

    /**
     * 내 기본 이력서를 조회한다.
     * 기본 이력서가 없으면 resumeData = null 로 응답한다.
     */
    public ResumeResponse getMyResume(User user) {
        return userResumeRepository.findByUserIdAndIsDefaultTrue(user.getId())
            .map(this::toResponse)
            .orElse(new ResumeResponse(null, user.getId(), false, null, null));
    }

    /**
     * 내 기본 이력서를 저장(upsert)한다.
     * 기존 기본 이력서가 있으면 업데이트, 없으면 새로 생성하고 기본으로 설정한다.
     */
    @Transactional
    public ResumeResponse saveMyResume(User user, ResumeSaveRequest request) {
        ResumeSaveRequest sanitized = sanitizeCertifications(request);
        String json = serializeToJson(sanitized);

        UserResume resume = userResumeRepository.findByUserIdAndIsDefaultTrue(user.getId())
            .map(existing -> {
                existing.updateResumeData(json);
                return existing;
            })
            .orElseGet(() -> UserResume.builder()
                .user(user)
                .resumeData(json)
                .isDefault(true)
                .build());

        UserResume saved = userResumeRepository.save(resume);
        log.info("기본 이력서 저장 완료: userId={}, resumeId={}", user.getId(), saved.getId());
        return toResponse(saved);
    }

    /**
     * 특정 사용자의 공개 기본 이력서를 조회한다.
     * 기본 이력서가 없거나 비공개이면 404.
     */
    public ResumeResponse getPublicResume(Long userId) {
        UserResume resume = userResumeRepository.findByUserIdAndIsDefaultTrue(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (!isPublic(resume.getResumeData())) {
            throw new GlobalException(ExceptionMessage.NOT_FOUND);
        }
        return toResponse(resume);
    }

    /**
     * 특정 사용자의 특정 공개 이력서를 조회한다.
     * 해당 이력서가 없거나 비공개이면 404.
     */
    public ResumeResponse getPublicResumeById(Long userId, Long resumeId) {
        UserResume resume = userResumeRepository.findByIdAndUserId(resumeId, userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (!isPublic(resume.getResumeData())) {
            throw new GlobalException(ExceptionMessage.NOT_FOUND);
        }
        return toResponse(resume);
    }

    // ── 다중 이력서 API ───────────────────────────────────────────────

    /**
     * 내 전체 이력서 목록을 최신 수정순으로 조회한다.
     */
    public ResumeListResponse getMyResumes(User user) {
        List<UserResume> resumes = userResumeRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getId());
        List<ResumeSummaryResponse> summaries = resumes.stream()
            .map(r -> ResumeSummaryResponse.from(r, objectMapper))
            .toList();
        return new ResumeListResponse(summaries, summaries.size());
    }

    /**
     * 새 이력서를 생성한다.
     * 첫 번째 이력서이면 자동으로 기본 이력서로 설정한다.
     */
    @Transactional
    public ResumeResponse createResume(User user, ResumeSaveRequest request) {
        ResumeSaveRequest sanitized = sanitizeCertifications(request);
        String json = serializeToJson(sanitized);

        boolean isFirst = userResumeRepository.countByUserId(user.getId()) == 0;

        UserResume resume = UserResume.builder()
            .user(user)
            .resumeData(json)
            .isDefault(isFirst)
            .build();

        UserResume saved = userResumeRepository.save(resume);
        log.info("이력서 생성 완료: userId={}, resumeId={}, isDefault={}", user.getId(), saved.getId(), saved.isDefault());
        return toResponse(saved);
    }

    /**
     * 특정 이력서 단건 조회 (본인 소유 검증).
     */
    public ResumeResponse getMyResumeById(User user, Long resumeId) {
        UserResume resume = userResumeRepository.findByIdAndUserId(resumeId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
        return toResponse(resume);
    }

    /**
     * 특정 이력서를 수정한다 (본인 소유 검증).
     */
    @Transactional
    public ResumeResponse updateResumeById(User user, Long resumeId, ResumeSaveRequest request) {
        UserResume resume = userResumeRepository.findByIdAndUserId(resumeId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        ResumeSaveRequest sanitized = sanitizeCertifications(request);
        String json = serializeToJson(sanitized);
        resume.updateResumeData(json);

        UserResume saved = userResumeRepository.save(resume);
        log.info("이력서 수정 완료: userId={}, resumeId={}", user.getId(), resumeId);
        return toResponse(saved);
    }

    /**
     * 특정 이력서를 삭제한다 (본인 소유 검증).
     * 기본 이력서를 삭제할 경우, 남은 이력서 중 가장 최근 것을 새 기본으로 승격한다.
     */
    @Transactional
    public void deleteResumeById(User user, Long resumeId) {
        UserResume resume = userResumeRepository.findByIdAndUserId(resumeId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        boolean wasDefault = resume.isDefault();
        userResumeRepository.delete(resume);

        if (wasDefault) {
            List<UserResume> remaining = userResumeRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getId());
            if (!remaining.isEmpty()) {
                remaining.get(0).setAsDefault();
                userResumeRepository.save(remaining.get(0));
                log.info("기본 이력서 자동 승격: userId={}, newDefaultId={}", user.getId(), remaining.get(0).getId());
            }
        }
        log.info("이력서 삭제 완료: userId={}, resumeId={}", user.getId(), resumeId);
    }

    /**
     * 특정 이력서를 기본 이력서로 설정한다 (본인 소유 검증).
     * 기존 기본 이력서는 해제된다.
     */
    @Transactional
    public ResumeResponse setDefaultResume(User user, Long resumeId) {
        UserResume resume = userResumeRepository.findByIdAndUserId(resumeId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        userResumeRepository.clearDefaultByUserId(user.getId());
        resume.setAsDefault();
        UserResume saved = userResumeRepository.save(resume);

        log.info("기본 이력서 설정: userId={}, resumeId={}", user.getId(), resumeId);
        return toResponse(saved);
    }

    // ── private helpers ──────────────────────────────────────────────

    private ResumeResponse toResponse(UserResume resume) {
        Object parsed = parseJson(resume.getResumeData());
        return new ResumeResponse(
            resume.getId(),
            resume.getUser().getId(),
            resume.isDefault(),
            parsed,
            resume.getUpdatedAt()
        );
    }

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
            request.isPublic(),
            request.visibleSections()
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
