package io.swkoreatech.kosp.domain.admin.role.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.role.api.AdminRoleApi;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyAssignRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.RoleRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.RoleUpdateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.response.RoleResponse;
import io.swkoreatech.kosp.domain.admin.role.service.RoleAdminService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminRoleController implements AdminRoleApi {

    private final RoleAdminService roleAdminService;

    @Override
    @Permit(name = "admin:roles:read", description = "역할 목록 조회")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleAdminService.getAllRoles());
    }

    @Override
    @Permit(name = "admin:roles:read", description = "역할 단일 조회")
    public ResponseEntity<RoleResponse> getRole(String roleName) {
        return ResponseEntity.ok(roleAdminService.getRole(roleName));
    }

    @Override
    @Permit(name = "admin:roles:create", description = "역할 생성")
    public ResponseEntity<Void> createRole(RoleRequest request) {
        roleAdminService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Permit(name = "admin:roles:update", description = "역할 수정")
    public ResponseEntity<Void> updateRole(String roleName, RoleUpdateRequest request) {
        roleAdminService.updateRole(roleName, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:roles:delete", description = "역할 삭제")
    public ResponseEntity<Void> deleteRole(String roleName) {
        roleAdminService.deleteRole(roleName);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:roles:assign-policy", description = "역할에 정책 할당")
    public ResponseEntity<Void> assignPolicy(String roleName, PolicyAssignRequest request) {
        roleAdminService.assignPolicy(roleName, request.policyName());
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:roles:remove-policy", description = "역할에서 정책 제거")
    public ResponseEntity<Void> removePolicy(String roleName, String policyName) {
        roleAdminService.removePolicy(roleName, policyName);
        return ResponseEntity.noContent().build();
    }
}
