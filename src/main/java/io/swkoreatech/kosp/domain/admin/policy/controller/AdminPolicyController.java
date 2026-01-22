package io.swkoreatech.kosp.domain.admin.policy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.policy.api.AdminPolicyApi;
import io.swkoreatech.kosp.domain.admin.policy.service.PolicyAdminService;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PermissionAssignRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyUpdateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PolicyResponse;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminPolicyController implements AdminPolicyApi {

    private final PolicyAdminService policyAdminService;

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
}
