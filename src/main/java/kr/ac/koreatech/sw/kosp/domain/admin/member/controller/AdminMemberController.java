package kr.ac.koreatech.sw.kosp.domain.admin.member.controller;

import kr.ac.koreatech.sw.kosp.domain.admin.member.api.AdminMemberApi;
import kr.ac.koreatech.sw.kosp.domain.admin.member.dto.request.AdminUserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.member.dto.request.UserRoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.service.AdminMemberService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminMemberController implements AdminMemberApi {

    private final AdminMemberService adminMemberService;

    @Override
    @Permit(name = "admin:users:delete", description = "사용자 강제 탈퇴")
    public ResponseEntity<Void> deleteUser(Long userId) {
        adminMemberService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:users:update", description = "사용자 정보 수정")
    public ResponseEntity<Void> updateUser(Long userId, AdminUserUpdateRequest request) {
        adminMemberService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:users:update-roles", description = "사용자 역할 변경")
    public ResponseEntity<Void> updateUserRoles(Long userId, UserRoleUpdateRequest request) {
        adminMemberService.updateUserRoles(userId, request.roles());
        return ResponseEntity.ok().build();
    }
}
