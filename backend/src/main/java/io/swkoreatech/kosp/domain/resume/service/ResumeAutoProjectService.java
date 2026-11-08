package io.swkoreatech.kosp.domain.resume.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.material.model.MaterialItem;
import io.swkoreatech.kosp.domain.material.repository.MaterialItemRepository;
import io.swkoreatech.kosp.domain.resume.dto.request.AutoProjectUpdateRequest;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeAutoProjectResponse;
import io.swkoreatech.kosp.domain.resume.model.AutoProjectOverrides;
import io.swkoreatech.kosp.domain.resume.model.ResumeMaterialProject;
import io.swkoreatech.kosp.domain.resume.model.UserResume;
import io.swkoreatech.kosp.domain.resume.repository.ResumeMaterialProjectRepository;
import io.swkoreatech.kosp.domain.resume.repository.UserResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이력서 자동 프로젝트 서비스 (과제/EL 자료 → 이력서 프로젝트 자동 연결).
 *
 * <p>자동 프로젝트는 {@link MaterialItem}(auto_imported=true)에서 <b>실시간 투영</b>으로 계산한다.
 * 원본 자료가 바뀌면 별도 동기화 없이 즉시 반영된다.
 * {@link ResumeMaterialProject} 는 그 투영에 대한 <b>예외</b>(삭제 tombstone / 사용자 수정 / 공개 override)만 저장한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeAutoProjectService {

    private final UserResumeRepository userResumeRepository;
    private final MaterialItemRepository materialItemRepository;
    private final ResumeMaterialProjectRepository linkRepository;
    private final ObjectMapper objectMapper;

    /**
     * 특정 이력서의 자동 프로젝트 목록을 실시간 투영으로 반환한다.
     * 삭제(tombstone)된 항목도 {@code deletedByUser=true} 로 함께 반환하여 복원 UI를 지원한다.
     */
    public List<ResumeAutoProjectResponse> getAutoProjects(User user, Long resumeId) {
        getOwnedResume(user, resumeId);

        Map<Long, ResumeMaterialProject> linksByItemId = linkRepository.findAllByResumeId(resumeId).stream()
            .collect(Collectors.toMap(link -> link.getMaterialItem().getId(), Function.identity()));

        return materialItemRepository
            .findAllByUserIdAndAutoImportedTrueOrderBySemesterOrderDescMaterialDateDescIdDesc(user.getId()).stream()
            .map(item -> toResponse(item, linksByItemId.get(item.getId())))
            .toList();
    }

    /**
     * 자동 프로젝트를 삭제 처리한다(tombstone). 재동기화로 부활하지 않는다.
     */
    @Transactional
    public void deleteAutoProject(User user, Long resumeId, Long materialItemId) {
        ResumeMaterialProject link = getOrCreateLink(user, resumeId, materialItemId);
        link.markDeleted();
        log.info("자동 프로젝트 삭제(tombstone): userId={}, resumeId={}, materialItemId={}",
            user.getId(), resumeId, materialItemId);
    }

    /**
     * 삭제된 자동 프로젝트를 복원한다.
     */
    @Transactional
    public void restoreAutoProject(User user, Long resumeId, Long materialItemId) {
        getOwnedResume(user, resumeId);
        linkRepository.findByResumeIdAndMaterialItemId(resumeId, materialItemId)
            .ifPresent(ResumeMaterialProject::restore);
        log.info("자동 프로젝트 복원: userId={}, resumeId={}, materialItemId={}",
            user.getId(), resumeId, materialItemId);
    }

    /**
     * 자동 프로젝트를 사용자 수정본으로 덮어쓴다. 이후 원본 재동기화가 이 필드를 덮어쓰지 않는다.
     */
    @Transactional
    public ResumeAutoProjectResponse updateAutoProject(
        User user, Long resumeId, Long materialItemId, AutoProjectUpdateRequest request
    ) {
        ResumeMaterialProject link = getOrCreateLink(user, resumeId, materialItemId);

        AutoProjectOverrides overrides = new AutoProjectOverrides(
            request.name(), request.period(), request.summary(), request.docLink());
        link.applyOverrides(serialize(overrides));
        if (request.visibility() != null) {
            link.changeVisibility(request.visibility());
        }
        log.info("자동 프로젝트 수정: userId={}, resumeId={}, materialItemId={}",
            user.getId(), resumeId, materialItemId);
        return toResponse(link.getMaterialItem(), link);
    }

    // ── private helpers ───────────────────────────────────────────────

    /**
     * 자료 + (선택적)예외 링크를 프로젝트로 투영한다. overrides 가 있으면 원본 위에 덮어쓴다.
     */
    private ResumeAutoProjectResponse toResponse(MaterialItem item, ResumeMaterialProject link) {
        AutoProjectOverrides ov = parseOverrides(link);
        String name = firstNonBlank(ov == null ? null : ov.name(), item.getTitle());
        String period = firstNonBlank(ov == null ? null : ov.period(), buildPeriod(item));
        String summary = firstNonBlank(ov == null ? null : ov.summary(), item.getSubjectName());
        String baseDoc = item.getFileUrl() != null ? item.getFileUrl() : item.getSourceUrl();
        String docLink = firstNonBlank(ov == null ? null : ov.docLink(), baseDoc);

        return new ResumeAutoProjectResponse(
            item.getId(),
            item.getSource(),
            name,
            period,
            summary,
            docLink,
            item.isDuplicatedWithGithub(),
            item.getDuplicateRepoKey(),
            true,
            link != null && link.isUserEdited(),
            link != null && link.isDeletedByUser(),
            link != null && link.isPublic()
        );
    }

    private ResumeMaterialProject getOrCreateLink(User user, Long resumeId, Long materialItemId) {
        UserResume resume = getOwnedResume(user, resumeId);
        MaterialItem item = materialItemRepository.findByIdAndUserId(materialItemId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
        return linkRepository.findByResumeIdAndMaterialItemId(resumeId, materialItemId)
            .orElseGet(() -> linkRepository.save(ResumeMaterialProject.builder()
                .resume(resume)
                .materialItem(item)
                .sourceType(item.getSource())
                .lastSyncedAt(LocalDateTime.now())
                .build()));
    }

    private UserResume getOwnedResume(User user, Long resumeId) {
        return userResumeRepository.findByIdAndUserId(resumeId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    private AutoProjectOverrides parseOverrides(ResumeMaterialProject link) {
        if (link == null || link.getOverrides() == null || link.getOverrides().isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(link.getOverrides(), AutoProjectOverrides.class);
        } catch (JsonProcessingException e) {
            log.warn("자동 프로젝트 overrides 파싱 실패: linkId={}", link.getId(), e);
            return null;
        }
    }

    private String serialize(AutoProjectOverrides overrides) {
        try {
            return objectMapper.writeValueAsString(overrides);
        } catch (JsonProcessingException e) {
            throw new GlobalException(ExceptionMessage.SERVER_ERROR);
        }
    }

    private String buildPeriod(MaterialItem item) {
        Integer year = item.getMaterialYear();
        String semester = item.getSemester();
        if (year == null) {
            return semester == null ? null : semester;
        }
        return semester == null ? String.valueOf(year) : year + " " + semester;
    }

    private String firstNonBlank(String override, String fallback) {
        return override != null && !override.isBlank() ? override : fallback;
    }
}
