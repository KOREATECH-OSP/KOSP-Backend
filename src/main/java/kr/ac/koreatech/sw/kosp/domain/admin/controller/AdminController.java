package kr.ac.koreatech.sw.kosp.domain.admin.controller;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.api.AdminApi;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.NoticeCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.PolicyAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.UserRoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.AdminUserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.response.RoleResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.service.AdminMemberService;
import kr.ac.koreatech.sw.kosp.domain.admin.service.AdminContentService;
import kr.ac.koreatech.sw.kosp.domain.admin.service.RoleAdminService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.challenge.service.ChallengeService;
import kr.ac.koreatech.sw.kosp.domain.challenge.dto.request.ChallengeRequest;
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
    private final AdminContentService adminContentService;
    private final ChallengeService challengeService;

    @Override
    @Permit(name = "admin:users:delete", description = "사용자 강제 탈퇴")
    public ResponseEntity<Void> deleteUser(Long userId) {
        adminMemberService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:articles:delete", description = "게시글 삭제")
    public ResponseEntity<Void> deleteArticle(Long articleId) {
        adminContentService.deleteArticle(articleId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:notices:delete", description = "공지 삭제")
    public ResponseEntity<Void> deleteNotice(Long noticeId) {
        adminContentService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:notices:create", description = "공지사항 작성")
    public ResponseEntity<Void> createNotice(User user, NoticeCreateRequest request) {
        adminContentService.createNotice(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

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

    @Override
    @Permit(name = "admin:challenges:create", description = "챌린지 생성")
    public ResponseEntity<Void> createChallenge(ChallengeRequest request) {
        challengeService.createChallenge(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Permit(name = "admin:challenges:delete", description = "챌린지 삭제")
    public ResponseEntity<Void> deleteChallenge(Long challengeId) {
        challengeService.deleteChallenge(challengeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:users:update", description = "사용자 정보 수정")
    public ResponseEntity<Void> updateUser(Long userId, AdminUserUpdateRequest request) {
        adminMemberService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:challenges:update", description = "챌린지 수정")
    public ResponseEntity<Void> updateChallenge(Long challengeId, ChallengeRequest request) {
        challengeService.updateChallenge(challengeId, request);
        return ResponseEntity.ok().build();
    }
}
