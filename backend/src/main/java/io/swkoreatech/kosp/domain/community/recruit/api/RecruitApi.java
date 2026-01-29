package io.swkoreatech.kosp.domain.community.recruit.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitStatusRequest;
import jakarta.validation.Valid;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyDecisionRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitApplyListResponse;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitApplyResponse;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitResponse;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

@Tag(name = "Recruit", description = "모집 공고 관리 API")
public interface RecruitApi {

    @Operation(summary = "모집 공고 목록 조회", description = "전체 모집 공고 목록을 조회합니다. RSQL filter로 필터링 가능 (예: isDeleted==false, status==OPEN)")
    @GetMapping
    ResponseEntity<RecruitListResponse> getList(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestParam Long boardId,
        @Parameter(description = "RSQL 필터 (예: isDeleted==false, status==OPEN)") @RequestParam(required = false) String rsql,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "모집 공고 상세 조회", description = "모집 공고 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    ResponseEntity<RecruitResponse> getOne(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );

    @Operation(summary = "모집 공고 작성", description = "새로운 모집 공고를 작성합니다.")
    @PostMapping
    ResponseEntity<Void> create(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid RecruitRequest request
    );

    @Operation(summary = "모집 공고 수정", description = "기존 모집 공고를 수정합니다.")
    @PutMapping("/{id}")
    ResponseEntity<Void> update(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid RecruitRequest request
    );

    @Operation(summary = "모집 공고 삭제", description = "모집 공고를 삭제합니다.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );

    @Operation(summary = "모집 상태 변경", description = "모집 공고의 상태를 변경합니다.")
    @PatchMapping("/{id}/status")
    ResponseEntity<Void> updateStatus(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid RecruitStatusRequest request
    );

    @Operation(summary = "모집 공고 지원", description = "모집 공고에 지원합니다.")
    @PostMapping("/{recruitId}/apply")
    ResponseEntity<Void> applyRecruit(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long recruitId,
        @RequestBody @Valid RecruitApplyRequest request
    );

    @Operation(summary = "지원자 목록 조회", description = "모집 공고에 대한 지원자 목록을 조회합니다. (팀장 전용) RSQL filter로 필터링 가능 (예: status==PENDING)")
    @GetMapping("/{recruitId}/applications")
    ResponseEntity<RecruitApplyListResponse> getApplicants(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long recruitId,
        @Parameter(description = "RSQL 필터 (예: status==PENDING, status==ACCEPTED)") @RequestParam(required = false) String filter,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "지원 상세 조회", description = "지원 상세 정보를 조회합니다. (팀장 전용)")
    @GetMapping("/applications/{applicationId}")
    ResponseEntity<RecruitApplyResponse> getApplication(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long applicationId
    );

    @Operation(summary = "지원 수락/거절", description = "지원을 수락하거나 거절합니다. (팀장 전용)")
    @PatchMapping("/applications/{applicationId}")
    ResponseEntity<Void> decideApplication(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long applicationId,
        @RequestBody @Valid RecruitApplyDecisionRequest request
    );
}

