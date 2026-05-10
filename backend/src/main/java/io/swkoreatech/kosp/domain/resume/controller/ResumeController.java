package io.swkoreatech.kosp.domain.resume.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.resume.api.ResumeApi;
import io.swkoreatech.kosp.domain.resume.dto.request.ResumeSaveRequest;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeResponse;
import io.swkoreatech.kosp.domain.resume.service.ResumeService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 이력서 관리 컨트롤러.
 * {@link ResumeApi}의 구현체로, 내 이력서 조회/저장 및 공개 이력서 조회를 처리한다.
 */
@RestController
@RequiredArgsConstructor
public class ResumeController implements ResumeApi {

    private final ResumeService resumeService;

    /** {@inheritDoc} */
    @Override
    @Permit(description = "내 이력서 조회")
    public ResponseEntity<ResumeResponse> getMyResume(@AuthUser User user) {
        return ResponseEntity.ok(resumeService.getMyResume(user));
    }

    /** {@inheritDoc} */
    @Override
    @Permit(description = "내 이력서 저장")
    public ResponseEntity<ResumeResponse> saveMyResume(@AuthUser User user, ResumeSaveRequest request) {
        return ResponseEntity.ok(resumeService.saveMyResume(user, request));
    }

    /** {@inheritDoc} */
    @Override
    @Permit(permitAll = true, name = "resume:public", description = "공개 이력서 조회")
    public ResponseEntity<ResumeResponse> getPublicResume(@PathVariable Long userId) {
        return ResponseEntity.ok(resumeService.getPublicResume(userId));
    }
}
