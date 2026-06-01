package io.swkoreatech.kosp.domain.resume.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.resume.dto.request.ResumeSaveRequest;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeListResponse;
import io.swkoreatech.kosp.domain.resume.dto.response.ResumeResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 이력서 관리 API.
 * 기존 단일 이력서 API(하위 호환)와 다중 이력서 API를 함께 제공한다.
 */
@Tag(name = "Resume", description = "이력서 관리 API")
public interface ResumeApi {

    // ── 하위 호환 API (기본 이력서 기준) ──────────────────────────────

    @Operation(summary = "내 기본 이력서 조회 (하위 호환)",
        description = "기본(default) 이력서를 반환합니다. 없으면 resumeData=null.")
    @GetMapping("/v1/users/me/resume")
    ResponseEntity<ResumeResponse> getMyResume(
        @Parameter(hidden = true) @AuthUser User user
    );

    @Operation(summary = "내 기본 이력서 저장 (하위 호환)",
        description = "기본 이력서 upsert. 기존 기본 이력서가 있으면 update, 없으면 create.")
    @PostMapping("/v1/users/me/resume")
    ResponseEntity<ResumeResponse> saveMyResume(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid ResumeSaveRequest request
    );

    @Operation(summary = "공개 기본 이력서 조회 (하위 호환)",
        description = "특정 사용자의 공개 기본 이력서. 비공개이거나 없으면 404.")
    @GetMapping("/v1/users/{userId}/resume")
    ResponseEntity<ResumeResponse> getPublicResume(
        @PathVariable Long userId
    );

    // ── 다중 이력서 API ───────────────────────────────────────────────

    @Operation(summary = "내 이력서 목록 조회",
        description = "로그인한 사용자의 전체 이력서 목록을 최신 수정순으로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/v1/users/me/resumes")
    ResponseEntity<ResumeListResponse> getMyResumes(
        @Parameter(hidden = true) @AuthUser User user
    );

    @Operation(summary = "이력서 생성",
        description = "새 이력서를 생성합니다. 첫 번째 이력서는 자동으로 기본 이력서가 됩니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping("/v1/users/me/resumes")
    ResponseEntity<ResumeResponse> createResume(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid ResumeSaveRequest request
    );

    @Operation(summary = "특정 이력서 단건 조회",
        description = "본인 소유의 특정 이력서를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "이력서 없음 또는 소유 불일치")
    @GetMapping("/v1/users/me/resumes/{resumeId}")
    ResponseEntity<ResumeResponse> getMyResumeById(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long resumeId
    );

    @Operation(summary = "특정 이력서 수정",
        description = "본인 소유의 특정 이력서를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "이력서 없음 또는 소유 불일치")
    @PutMapping("/v1/users/me/resumes/{resumeId}")
    ResponseEntity<ResumeResponse> updateResumeById(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long resumeId,
        @RequestBody @Valid ResumeSaveRequest request
    );

    @Operation(summary = "특정 이력서 삭제",
        description = "본인 소유의 특정 이력서를 삭제합니다. 기본 이력서 삭제 시 남은 이력서 중 최신 것이 자동 승격됩니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "이력서 없음 또는 소유 불일치")
    @DeleteMapping("/v1/users/me/resumes/{resumeId}")
    ResponseEntity<Void> deleteResumeById(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long resumeId
    );

    @Operation(summary = "기본 이력서 설정",
        description = "특정 이력서를 기본(default) 이력서로 설정합니다. 기존 기본 이력서는 자동 해제됩니다.")
    @ApiResponse(responseCode = "200", description = "설정 성공")
    @ApiResponse(responseCode = "404", description = "이력서 없음 또는 소유 불일치")
    @PatchMapping("/v1/users/me/resumes/{resumeId}/default")
    ResponseEntity<ResumeResponse> setDefaultResume(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long resumeId
    );

    @Operation(summary = "공개 이력서 단건 조회 (resumeId 지정)",
        description = "특정 사용자의 특정 이력서 공개 조회. 비공개이거나 없으면 404.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/v1/users/{userId}/resumes/{resumeId}")
    ResponseEntity<ResumeResponse> getPublicResumeById(
        @PathVariable Long userId,
        @PathVariable Long resumeId
    );
}
