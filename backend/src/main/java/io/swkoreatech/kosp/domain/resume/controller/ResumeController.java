package io.swkoreatech.kosp.domain.resume.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.resume.api.ResumeApi;
import io.swkoreatech.kosp.domain.resume.dto.request.ResumeSaveRequest;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeListResponse;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeResponse;
import io.swkoreatech.kosp.domain.resume.service.ResumeService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 이력서 관리 컨트롤러.
 * {@link ResumeApi}의 구현체로, 단일 이력서(하위 호환) 및 다중 이력서 API를 처리한다.
 */
@RestController
@RequiredArgsConstructor
public class ResumeController implements ResumeApi {

    private final ResumeService resumeService;

    // ── 하위 호환 API ──────────────────────────────────────────────

    @Override
    @Permit(description = "내 기본 이력서 조회")
    public ResponseEntity<ResumeResponse> getMyResume(@AuthUser User user) {
        return ResponseEntity.ok(resumeService.getMyResume(user));
    }

    @Override
    @Permit(description = "내 기본 이력서 저장")
    public ResponseEntity<ResumeResponse> saveMyResume(@AuthUser User user, ResumeSaveRequest request) {
        return ResponseEntity.ok(resumeService.saveMyResume(user, request));
    }

    @Override
    @Permit(permitAll = true, description = "공개 기본 이력서 조회")
    public ResponseEntity<ResumeResponse> getPublicResume(@PathVariable Long userId) {
        return ResponseEntity.ok(resumeService.getPublicResume(userId));
    }

    // ── 다중 이력서 API ───────────────────────────────────────────

    @Override
    @Permit(description = "내 이력서 목록 조회")
    public ResponseEntity<ResumeListResponse> getMyResumes(@AuthUser User user) {
        return ResponseEntity.ok(resumeService.getMyResumes(user));
    }

    @Override
    @Permit(description = "이력서 생성")
    public ResponseEntity<ResumeResponse> createResume(@AuthUser User user, ResumeSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.createResume(user, request));
    }

    @Override
    @Permit(description = "특정 이력서 단건 조회")
    public ResponseEntity<ResumeResponse> getMyResumeById(@AuthUser User user, @PathVariable Long resumeId) {
        return ResponseEntity.ok(resumeService.getMyResumeById(user, resumeId));
    }

    @Override
    @Permit(description = "특정 이력서 수정")
    public ResponseEntity<ResumeResponse> updateResumeById(
        @AuthUser User user, @PathVariable Long resumeId, ResumeSaveRequest request
    ) {
        return ResponseEntity.ok(resumeService.updateResumeById(user, resumeId, request));
    }

    @Override
    @Permit(description = "특정 이력서 삭제")
    public ResponseEntity<Void> deleteResumeById(@AuthUser User user, @PathVariable Long resumeId) {
        resumeService.deleteResumeById(user, resumeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(description = "기본 이력서 설정")
    public ResponseEntity<ResumeResponse> setDefaultResume(@AuthUser User user, @PathVariable Long resumeId) {
        return ResponseEntity.ok(resumeService.setDefaultResume(user, resumeId));
    }

    @Override
    @Permit(permitAll = true, description = "공개 이력서 단건 조회 (resumeId 지정)")
    public ResponseEntity<ResumeResponse> getPublicResumeById(
        @PathVariable Long userId, @PathVariable Long resumeId
    ) {
        return ResponseEntity.ok(resumeService.getPublicResumeById(userId, resumeId));
    }
}
