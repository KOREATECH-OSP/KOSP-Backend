package kr.ac.koreatech.sw.kosp.domain.admin.role.controller;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.role.api.AdminRoleApi;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleRequest;
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
    @Permit(name = "admin:roles:create", description = "역할 생성")
    public ResponseEntity<Void> createRole(RoleRequest request) {
        roleAdminService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Permit(name = "admin:roles:assign-policy", description = "역할에 정책 할당")
    public ResponseEntity<Void> assignPolicy(String roleName, PolicyAssignRequest request) {
        roleAdminService.assignPolicy(roleName, request.policyName());
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:policies:read", description = "정책 목록 조회")
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        return ResponseEntity.ok(policyAdminService.getAllPolicies());
    }

    @Override
    @Permit(name = "admin:policies:create", description = "정책 생성")
    public ResponseEntity<Void> createPolicy(PolicyCreateRequest request) {
        policyAdminService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Permit(name = "admin:permissions:read", description = "권한 목록 조회")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(policyAdminService.getAllPermissions());
    }
}
