package io.swkoreatech.kosp.domain.organization.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.dto.request.OrganizationRegisterRequest;
import io.swkoreatech.kosp.domain.organization.dto.response.AvailableOrganizationResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationDetailResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

@Tag(name = "Organization", description = "GitHub 조직 등록 및 조회 API")
public interface OrganizationApi {

    @Operation(
        summary = "등록 가능한 조직 목록 조회",
        description = "로그인 사용자가 GitHub에서 Owner 권한을 가진 조직 목록을 조회합니다. read:org 권한이 없으면 재인증이 필요합니다."
    )
    @GetMapping("/available")
    ResponseEntity<List<AvailableOrganizationResponse>> getAvailable(
        @Parameter(hidden = true) @AuthUser User user
    );

    @Operation(
        summary = "조직 등록",
        description = "선택한 GitHub Organization을 K-OSP에 등록합니다. 멤버와 저장소를 자동으로 수집합니다."
    )
    @PostMapping
    ResponseEntity<OrganizationResponse> register(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid OrganizationRegisterRequest request
    );

    @Operation(
        summary = "내가 등록한 조직 목록 조회",
        description = "로그인 사용자가 등록한 조직 목록을 조회합니다."
    )
    @GetMapping("/my")
    ResponseEntity<List<OrganizationResponse>> getMyOrganizations(
        @Parameter(hidden = true) @AuthUser User user
    );

    @Operation(
        summary = "조직 상세 조회",
        description = "조직 기본 정보, 멤버 수, 저장소 수를 조회합니다."
    )
    @GetMapping("/{organizationId}")
    ResponseEntity<OrganizationDetailResponse> getDetail(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long organizationId
    );
}
