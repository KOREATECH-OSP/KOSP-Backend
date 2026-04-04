package io.swkoreatech.kosp.domain.admin.title.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleGrantRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleRevokeRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.response.AdminTitleBatchLogListResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 관리자 전용 칭호 관리 API 인터페이스.
 */
@Tag(name = "Admin - Title", description = "관리자 전용 칭호 수동 지급/회수 및 배치 로그 조회 API")
@RequestMapping("/v1/admin/titles")
public interface AdminTitleApi {

    /**
     * 특정 유저에게 칭호를 수동 지급한다.
     *
     * @param request 지급 요청
     * @param admin   조작한 관리자
     * @return 201 Created
     */
    @Operation(
        summary = "칭호 수동 지급",
        description = "관리자 권한으로 특정 유저에게 칭호를 수동으로 지급합니다. 이력이 기록됩니다."
    )
    @ApiResponse(responseCode = "201", description = "지급 성공")
    @ApiResponse(responseCode = "404", description = "유저 또는 칭호를 찾을 수 없음")
    @ApiResponse(responseCode = "409", description = "이미 보유 중인 칭호")
    @PostMapping("/grant")
    ResponseEntity<Void> grantTitle(@RequestBody @Valid AdminTitleGrantRequest request, @AuthUser User admin);

    /**
     * 유저의 칭호를 회수한다 (소프트 회수).
     *
     * @param request 회수 요청
     * @param admin   조작한 관리자
     * @return 200 OK
     */
    @Operation(
        summary = "칭호 회수",
        description = "관리자 권한으로 유저의 칭호를 회수합니다. 이력이 기록되며 복구 가능합니다."
    )
    @ApiResponse(responseCode = "200", description = "회수 성공")
    @ApiResponse(responseCode = "400", description = "이미 회수된 칭호")
    @ApiResponse(responseCode = "404", description = "유저 칭호를 찾을 수 없음")
    @PostMapping("/revoke")
    ResponseEntity<Void> revokeTitle(@RequestBody @Valid AdminTitleRevokeRequest request, @AuthUser User admin);

    /**
     * 칭호 배치 실행 이력을 조회한다.
     *
     * @param pageable 페이징 정보
     * @return 배치 로그 목록
     */
    @Operation(
        summary = "배치 실행 이력 조회",
        description = "칭호 평가 배치 실행 이력을 최신 순으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/batch-logs")
    ResponseEntity<AdminTitleBatchLogListResponse> getBatchLogs(
        @PageableDefault(size = 20) Pageable pageable
    );
}
