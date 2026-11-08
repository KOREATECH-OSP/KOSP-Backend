package io.swkoreatech.kosp.domain.admin.organization.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.organization.api.AdminOrganizationApi;
import io.swkoreatech.kosp.domain.admin.organization.dto.response.AdminOrganizationMemberResponse;
import io.swkoreatech.kosp.domain.admin.organization.dto.response.AdminOrganizationRepoResponse;
import io.swkoreatech.kosp.domain.admin.organization.service.AdminOrganizationService;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationResponse;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/organizations")
public class AdminOrganizationController implements AdminOrganizationApi {

    private final AdminOrganizationService adminOrganizationService;

    @Override
    @GetMapping
    @Permit(name = "admin:org:list", description = "관리자 전체 조직 목록 조회")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        return ResponseEntity.ok(adminOrganizationService.getAllOrganizations());
    }

    @Override
    @GetMapping("/{organizationId}/members")
    @Permit(name = "admin:org:members", description = "관리자 조직 멤버 목록 조회")
    public ResponseEntity<List<AdminOrganizationMemberResponse>> getMembers(
        @PathVariable Long organizationId
    ) {
        return ResponseEntity.ok(adminOrganizationService.getMembers(organizationId));
    }

    @Override
    @GetMapping("/{organizationId}/repositories")
    @Permit(name = "admin:org:repos", description = "관리자 조직 저장소 목록 조회")
    public ResponseEntity<List<AdminOrganizationRepoResponse>> getRepositories(
        @PathVariable Long organizationId
    ) {
        return ResponseEntity.ok(adminOrganizationService.getRepositories(organizationId));
    }

    @Override
    @PostMapping("/{organizationId}/sync")
    @Permit(name = "admin:org:sync", description = "관리자 조직 동기화")
    public ResponseEntity<Void> sync(@PathVariable Long organizationId) {
        adminOrganizationService.sync(organizationId);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{organizationId}")
    @Permit(name = "admin:org:deactivate", description = "관리자 조직 비활성화")
    public ResponseEntity<Void> deactivate(@PathVariable Long organizationId) {
        adminOrganizationService.deactivate(organizationId);
        return ResponseEntity.noContent().build();
    }
}
