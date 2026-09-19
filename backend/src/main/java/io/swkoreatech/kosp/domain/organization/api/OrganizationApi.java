package io.swkoreatech.kosp.domain.organization.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.dto.request.OrganizationAddMemberRequest;
import io.swkoreatech.kosp.domain.organization.dto.request.OrganizationRegisterRequest;
import io.swkoreatech.kosp.domain.organization.dto.request.OrganizationUpdateRequest;
import io.swkoreatech.kosp.domain.organization.dto.response.AvailableOrganizationResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationDetailResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationMemberResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationResponse;
import io.swkoreatech.kosp.global.host.ClientURL;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

@Tag(name = "Organization", description = "GitHub 조직 등록 및 조회 API")
public interface OrganizationApi {

    @Operation(
        summary = "전체 조직 목록 조회",
        description = "K-OSP에 등록된 모든 활성 조직 목록을 조회합니다. search 파라미터로 이름 검색이 가능합니다."
    )
    @GetMapping
    ResponseEntity<List<OrganizationResponse>> getAllOrganizations(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestParam(required = false) String search
    );

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
        @RequestBody @Valid OrganizationRegisterRequest request,
        @Parameter(hidden = true) @ClientURL String clientUrl
    );

    @Operation(
        summary = "내가 등록한 조직 목록 조회",
        description = "로그인 사용자가 속한 조직 목록을 조회합니다."
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

    @Operation(
        summary = "조직 정보 수정",
        description = "조직의 이름, 설명, 태그를 수정합니다. Owner만 가능합니다."
    )
    @PatchMapping("/{organizationId}")
    ResponseEntity<Void> updateOrganization(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long organizationId,
        @RequestBody @Valid OrganizationUpdateRequest request
    );

    @Operation(
        summary = "조직 멤버 추가",
        description = "GitHub 사용자명으로 조직 멤버를 추가합니다. Owner 또는 Admin만 가능합니다."
    )
    @PostMapping("/{organizationId}/members")
    ResponseEntity<OrganizationMemberResponse> addMember(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long organizationId,
        @RequestBody @Valid OrganizationAddMemberRequest request
    );
}
