package kr.ac.koreatech.sw.kosp.domain.admin.role.controller;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.role.api.AdminRoleApi;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PermissionAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PermissionUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PermissionResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PolicyResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.RoleResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.service.PolicyAdminService;
import kr.ac.koreatech.sw.kosp.domain.admin.service.RoleAdminService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminRoleController implements AdminRoleApi {

    private final RoleAdminService roleAdminService;
    private final PolicyAdminService policyAdminService;

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

    @Override
    @Permit(name = "admin:policies:read", description = "정책 목록 조회")
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        return ResponseEntity.ok(policyAdminService.getAllPolicies());
    }

    @Override
    @Permit(name = "admin:policies:read", description = "정책 단일 조회")
    public ResponseEntity<PolicyResponse> getPolicy(String policyName) {
        return ResponseEntity.ok(policyAdminService.getPolicy(policyName));
    }

    @Override
    @Permit(name = "admin:policies:create", description = "정책 생성")
    public ResponseEntity<Void> createPolicy(PolicyCreateRequest request) {
        policyAdminService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Permit(name = "admin:policies:update", description = "정책 수정")
    public ResponseEntity<Void> updatePolicy(String policyName, PolicyUpdateRequest request) {
        policyAdminService.updatePolicy(policyName, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:policies:delete", description = "정책 삭제")
    public ResponseEntity<Void> deletePolicy(String policyName) {
        policyAdminService.deletePolicy(policyName);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:policies:assign-permission", description = "정책에 권한 할당")
    public ResponseEntity<Void> assignPermissionToPolicy(String policyName, PermissionAssignRequest request) {
        policyAdminService.assignPermission(policyName, request.permissionName());
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:policies:remove-permission", description = "정책에서 권한 제거")
    public ResponseEntity<Void> removePermission(String policyName, String permissionName) {
        policyAdminService.removePermission(policyName, permissionName);
        return ResponseEntity.noContent().build();
    }

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

    @Override
    @Permit(name = "admin:permissions:update", description = "권한 수정")
    public ResponseEntity<Void> updatePermission(String permissionName, PermissionUpdateRequest request) {
        policyAdminService.updatePermission(permissionName, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:permissions:delete", description = "권한 삭제")
    public ResponseEntity<Void> deletePermission(String permissionName) {
        policyAdminService.deletePermission(permissionName);
        return ResponseEntity.noContent().build();
    }
}
