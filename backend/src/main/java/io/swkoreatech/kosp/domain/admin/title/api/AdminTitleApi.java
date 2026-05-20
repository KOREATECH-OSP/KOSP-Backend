package io.swkoreatech.kosp.domain.admin.title.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleGrantRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleRevokeRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleUpdateImageRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.response.AdminTitleBatchLogListResponse;
import io.swkoreatech.kosp.domain.admin.title.dto.response.AdminTitleImageResponse;
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
     * 칭호 아이콘 URL을 수정한다.
     *
     * @param titleId 칭호 PK
     * @param request 새 아이콘 URL (null 이면 초기화)
     * @return 200 OK
     */
    @Operation(
        summary = "칭호 아이콘 URL 수정 (URL 직접 입력)",
        description = "관리자 권한으로 칭호의 iconUrl을 직접 입력하여 수정합니다. null을 전달하면 초기화됩니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "칭호를 찾을 수 없음")
    @PatchMapping("/{titleId}/image")
    ResponseEntity<Void> updateTitleImage(
        @PathVariable Long titleId,
        @RequestBody @Valid AdminTitleUpdateImageRequest request
    );

    @Operation(
        summary = "칭호 이미지 파일 업로드",
        description = "관리자 권한으로 이미지 파일을 직접 업로드하여 칭호의 iconUrl을 설정합니다. "
            + "허용 형식: png, jpg, jpeg, webp / 최대 크기: 5MB"
    )
    @ApiResponse(responseCode = "200", description = "업로드 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 크기 초과")
    @ApiResponse(responseCode = "404", description = "칭호를 찾을 수 없음")
    @PostMapping(value = "/{titleId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<AdminTitleImageResponse> uploadTitleImage(
        @PathVariable Long titleId,
        @RequestParam("file") MultipartFile file
    );

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
