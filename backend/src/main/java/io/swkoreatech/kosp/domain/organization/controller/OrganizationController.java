package io.swkoreatech.kosp.domain.organization.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.api.OrganizationApi;
import io.swkoreatech.kosp.domain.organization.dto.request.OrganizationAddMemberRequest;
import io.swkoreatech.kosp.domain.organization.dto.request.OrganizationRegisterRequest;
import io.swkoreatech.kosp.domain.organization.dto.request.OrganizationUpdateRequest;
import io.swkoreatech.kosp.domain.organization.dto.response.AvailableOrganizationResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationDetailResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationMemberResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationRepoResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationResponse;
import io.swkoreatech.kosp.domain.organization.service.OrganizationService;
import io.swkoreatech.kosp.global.host.ClientURL;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/organizations")
public class OrganizationController implements OrganizationApi {

    private final OrganizationService organizationService;

    @Override
    @GetMapping
    @Permit(name = "org:all", description = "전체 조직 목록 조회")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations(
        @AuthUser User user,
        @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(organizationService.getAllOrganizations(search));
    }

    @Override
    @GetMapping("/available")
    @Permit(name = "org:available", description = "등록 가능한 조직 목록 조회")
    public ResponseEntity<List<AvailableOrganizationResponse>> getAvailable(@AuthUser User user) {
        return ResponseEntity.ok(organizationService.getAvailableOrganizations(user));
    }

    @Override
    @PostMapping
    @Permit(name = "org:register", description = "조직 등록")
    public ResponseEntity<OrganizationResponse> register(
        @AuthUser User user,
        @RequestBody @Valid OrganizationRegisterRequest request,
        @ClientURL String clientUrl
    ) {
        OrganizationResponse response = organizationService.registerOrganization(user, request.githubOrgId(), clientUrl);
        return ResponseEntity.created(URI.create("/v1/organizations/" + response.id())).body(response);
    }

    @Override
    @GetMapping("/my")
    @Permit(name = "org:my", description = "내가 속한 조직 목록 조회")
    public ResponseEntity<List<OrganizationResponse>> getMyOrganizations(@AuthUser User user) {
        return ResponseEntity.ok(organizationService.getMyOrganizations(user));
    }

    @Override
    @GetMapping("/{organizationId}")
    @Permit(name = "org:detail", description = "조직 상세 조회")
    public ResponseEntity<OrganizationDetailResponse> getDetail(
        @AuthUser User user,
        @PathVariable Long organizationId
    ) {
        return ResponseEntity.ok(organizationService.getDetail(organizationId));
    }

    @Override
    @PatchMapping("/{organizationId}")
    @Permit(name = "org:update", description = "조직 정보 수정 (Owner만 가능)")
    public ResponseEntity<Void> updateOrganization(
        @AuthUser User user,
        @PathVariable Long organizationId,
        @RequestBody @Valid OrganizationUpdateRequest request
    ) {
        organizationService.updateOrganization(organizationId, request, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{organizationId}/members")
    @Permit(name = "org:members", description = "조직 멤버 목록 조회")
    public ResponseEntity<List<OrganizationMemberResponse>> getMembers(
        @AuthUser User user,
        @PathVariable Long organizationId
    ) {
        return ResponseEntity.ok(organizationService.getMembers(organizationId, user));
    }

    @Override
    @PostMapping("/{organizationId}/members")
    @Permit(name = "org:members:add", description = "조직 멤버 추가 (Owner/Admin만 가능)")
    public ResponseEntity<OrganizationMemberResponse> addMember(
        @AuthUser User user,
        @PathVariable Long organizationId,
        @RequestBody @Valid OrganizationAddMemberRequest request
    ) {
        OrganizationMemberResponse response = organizationService.addMember(organizationId, request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{organizationId}/repositories")
    @Permit(name = "org:repositories", description = "조직 저장소 목록 조회")
    public ResponseEntity<List<OrganizationRepoResponse>> getRepositories(
        @AuthUser User user,
        @PathVariable Long organizationId
    ) {
        return ResponseEntity.ok(organizationService.getRepositories(organizationId, user));
    }

    @PostMapping("/{organizationId}/repositories/{repoId}/activate")
    @Permit(name = "org:repositories:activate", description = "조직 저장소 활성화 (Owner/Admin)")
    public ResponseEntity<Void> activateRepo(
        @AuthUser User user,
        @PathVariable Long organizationId,
        @PathVariable Long repoId
    ) {
        organizationService.activateRepo(organizationId, repoId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{organizationId}/repositories/{repoId}/activate")
    @Permit(name = "org:repositories:deactivate", description = "조직 저장소 비활성화 (Owner/Admin)")
    public ResponseEntity<Void> deactivateRepo(
        @AuthUser User user,
        @PathVariable Long organizationId,
        @PathVariable Long repoId
    ) {
        organizationService.deactivateRepo(organizationId, repoId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{organizationId}/members/{memberId}/admin")
    @Permit(name = "org:members:admin:appoint", description = "조직 멤버 관리자 임명")
    public ResponseEntity<Void> appointAdmin(
        @AuthUser User user,
        @PathVariable Long organizationId,
        @PathVariable Long memberId
    ) {
        organizationService.appointAdmin(organizationId, memberId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{organizationId}/members/{memberId}/admin")
    @Permit(name = "org:members:admin:dismiss", description = "조직 멤버 관리자 해임 (Owner만 가능)")
    public ResponseEntity<Void> dismissAdmin(
        @AuthUser User user,
        @PathVariable Long organizationId,
        @PathVariable Long memberId
    ) {
        organizationService.dismissAdmin(organizationId, memberId, user);
        return ResponseEntity.noContent().build();
    }
}
