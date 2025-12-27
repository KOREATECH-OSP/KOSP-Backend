package kr.ac.koreatech.sw.kosp.domain.admin.controller;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.api.AdminApi;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.PolicyAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.UserRoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.response.RoleResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.service.AdminMemberService;
import kr.ac.koreatech.sw.kosp.domain.admin.service.RoleAdminService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final RoleAdminService roleAdminService;
    private final AdminMemberService adminMemberService;

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
    @Permit(name = "admin:users:update-roles", description = "사용자 역할 변경")
    public ResponseEntity<Void> updateUserRoles(Long userId, UserRoleUpdateRequest request) {
        adminMemberService.updateUserRoles(userId, request.roles());
        return ResponseEntity.ok().build();
    }
}
