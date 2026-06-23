package io.swkoreatech.kosp.domain.admin.organization.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.admin.organization.dto.response.AdminOrganizationMemberResponse;
import io.swkoreatech.kosp.domain.admin.organization.dto.response.AdminOrganizationRepoResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationResponse;

@Tag(name = "Admin - Organization", description = "관리자 조직 관리 API")
public interface AdminOrganizationApi {

    @Operation(
        summary = "전체 조직 목록 조회",
        description = "등록된 모든 GitHub 조직 목록을 조회합니다."
    )
    @GetMapping
    ResponseEntity<List<OrganizationResponse>> getAllOrganizations();

    @Operation(
        summary = "조직 멤버 목록 조회",
        description = "특정 조직의 멤버 목록을 조회합니다."
    )
    @GetMapping("/{organizationId}/members")
    ResponseEntity<List<AdminOrganizationMemberResponse>> getMembers(
        @PathVariable Long organizationId
    );

    @Operation(
        summary = "조직 저장소 목록 조회",
        description = "특정 조직의 저장소 목록을 조회합니다."
    )
    @GetMapping("/{organizationId}/repositories")
    ResponseEntity<List<AdminOrganizationRepoResponse>> getRepositories(
        @PathVariable Long organizationId
    );

    @Operation(
        summary = "조직 동기화",
        description = "GitHub에서 최신 멤버 및 저장소 정보를 다시 수집합니다."
    )
    @PostMapping("/{organizationId}/sync")
    ResponseEntity<Void> sync(@PathVariable Long organizationId);

    @Operation(
        summary = "조직 비활성화",
        description = "조직과 K-OSP의 연결을 해제합니다."
    )
    @DeleteMapping("/{organizationId}")
    ResponseEntity<Void> deactivate(@PathVariable Long organizationId);
}
