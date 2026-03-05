package io.swkoreatech.kosp.domain.admin.permission.controller;

import io.swkoreatech.kosp.domain.admin.permission.api.AdminPermissionApi;
import io.swkoreatech.kosp.domain.admin.policy.service.PolicyAdminService;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PermissionResponse;
import io.swkoreatech.kosp.global.security.annotation.Permit;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 권한 조회 컨트롤러.
 * <p>{@link AdminPermissionApi}를 구현하여 권한 목록 및 단일 조회 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminPermissionController implements AdminPermissionApi {

    private final PolicyAdminService policyAdminService;

    @Override
    @Permit(name = "admin:permissions:read", description = "권한 목록 조회")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(policyAdminService.getAllPermissions());
    }

    @Override
    @Permit(name = "admin:permissions:read", description = "권한 단일 조회")
    public ResponseEntity<PermissionResponse> getPermission(String permissionName) {
        return ResponseEntity.ok(policyAdminService.getPermission(permissionName));
    }
}
