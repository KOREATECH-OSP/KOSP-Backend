package io.swkoreatech.kosp.domain.admin.permission.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PermissionResponse;

@Tag(name = "Admin - Permission", description = "관리자 전용 권한 조회 API")
@RequestMapping("/v1/admin/permissions")
public interface AdminPermissionApi {

    @Operation(summary = "권한 목록 조회", description = "시스템에 등록된 모든 권한(Permission)을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<List<PermissionResponse>> getAllPermissions();

    @Operation(summary = "권한 단일 조회", description = "특정 권한의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{permissionName}")
    ResponseEntity<PermissionResponse> getPermission(@Parameter(description = "권한 이름") @PathVariable String permissionName);
}
