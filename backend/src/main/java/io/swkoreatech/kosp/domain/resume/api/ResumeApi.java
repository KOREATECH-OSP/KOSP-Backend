package io.swkoreatech.kosp.domain.resume.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.resume.dto.request.ResumeSaveRequest;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 이력서 관리 API.
 * 내 이력서 조회/저장(upsert) 및 특정 사용자 공개 이력서 조회 기능을 정의한다.
 */
@Tag(name = "Resume", description = "이력서 관리 API")
public interface ResumeApi {

    /**
     * 내 이력서를 조회한다. (인증 필요)
     *
     * @param user 인증된 사용자
     * @return 이력서 응답 (저장 이력 없으면 resumeData = null)
     */
    @Operation(
        summary = "내 이력서 조회",
        description = "로그인한 사용자의 이력서를 조회합니다. 저장된 이력서가 없으면 resumeData가 null로 반환됩니다."
    )
    @GetMapping("/v1/users/me/resume")
    ResponseEntity<ResumeResponse> getMyResume(
        @Parameter(hidden = true) @AuthUser User user
    );

    /**
     * 내 이력서를 저장(upsert)한다. (인증 필요)
     *
     * @param user    인증된 사용자
     * @param request 저장 요청
     * @return 저장된 이력서 응답
     */
    @Operation(
        summary = "내 이력서 저장",
        description = "로그인한 사용자의 이력서를 저장합니다. 이미 존재하면 update, 없으면 create합니다."
    )
    @PostMapping("/v1/users/me/resume")
    ResponseEntity<ResumeResponse> saveMyResume(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid ResumeSaveRequest request
    );

    /**
     * 특정 사용자의 공개 이력서를 조회한다. (인증 불필요)
     *
     * <p>이력서가 비공개이거나 없으면 404를 반환한다.
     * 비공개 이력서 내용은 절대 응답하지 않는다.</p>
     *
     * @param userId 조회 대상 사용자 ID
     * @return 공개 이력서 응답
     */
    @Operation(
        summary = "공개 이력서 조회",
        description = "특정 사용자의 공개 이력서를 조회합니다. 비공개이거나 없으면 404를 반환합니다. 인증 없이 접근 가능합니다."
    )
    @GetMapping("/v1/users/{userId}/resume")
    ResponseEntity<ResumeResponse> getPublicResume(
        @PathVariable Long userId
    );
}
