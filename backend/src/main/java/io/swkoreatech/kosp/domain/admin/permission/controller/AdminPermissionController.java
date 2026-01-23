package io.swkoreatech.kosp.domain.admin.permission.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.permission.api.AdminPermissionApi;
import io.swkoreatech.kosp.domain.admin.policy.service.PolicyAdminService;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PermissionResponse;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

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
