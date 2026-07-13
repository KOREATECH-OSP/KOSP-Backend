package io.swkoreatech.kosp.domain.organization.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.api.OrganizationApi;
import io.swkoreatech.kosp.domain.organization.dto.request.OrganizationRegisterRequest;
import io.swkoreatech.kosp.domain.organization.dto.response.AvailableOrganizationResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationDetailResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationMemberResponse;
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
    @Permit(name = "org:my", description = "내가 등록한 조직 목록 조회")
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

    @GetMapping("/{organizationId}/members")
    @Permit(name = "org:members", description = "조직 멤버 목록 조회")
    public ResponseEntity<List<OrganizationMemberResponse>> getMembers(
        @AuthUser User user,
        @PathVariable Long organizationId
    ) {
        return ResponseEntity.ok(organizationService.getMembers(organizationId, user));
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
