package io.swkoreatech.kosp.domain.admin.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.member.dto.response.AdminUserListResponse;
import io.swkoreatech.kosp.domain.admin.member.api.AdminMemberApi;
import io.swkoreatech.kosp.domain.admin.member.dto.request.AdminUserUpdateRequest;
import io.swkoreatech.kosp.domain.admin.member.dto.request.UserRoleUpdateRequest;
import io.swkoreatech.kosp.domain.admin.member.service.AdminMemberService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminMemberController implements AdminMemberApi {

    private final AdminMemberService adminMemberService;

    @Override
    @Permit(name = "admin:users:read", description = "사용자 목록 조회")
    public ResponseEntity<AdminUserListResponse> getUsers(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        AdminUserListResponse response = adminMemberService.getUsers(pageable);
        return ResponseEntity.ok(response);
    }

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
