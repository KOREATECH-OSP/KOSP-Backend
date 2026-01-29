package io.swkoreatech.kosp.domain.admin.policy.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PermissionAssignRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyUpdateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PolicyDetailResponse;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PolicyResponse;

@Tag(name = "Admin - Policy", description = "관리자 전용 정책 관리 API")
@RequestMapping("/v1/admin/policies")
public interface AdminPolicyApi {

    @Operation(summary = "정책 목록 조회", description = "시스템에 등록된 모든 정책을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<List<PolicyResponse>> getAllPolicies();

    @Operation(summary = "정책 단일 조회", description = "특정 정책의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{policyName}")
    ResponseEntity<PolicyResponse> getPolicy(@Parameter(description = "정책 이름") @PathVariable String policyName);

    @Operation(summary = "정책 생성", description = "새로운 정책을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping
    ResponseEntity<Void> createPolicy(@RequestBody @Valid PolicyCreateRequest request);

    @Operation(summary = "정책 수정", description = "특정 정책의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/{policyName}")
    ResponseEntity<Void> updatePolicy(
        @Parameter(description = "정책 이름") @PathVariable String policyName,
        @RequestBody @Valid PolicyUpdateRequest request
    );

    @Operation(summary = "정책 삭제", description = "특정 정책을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{policyName}")
    ResponseEntity<Void> deletePolicy(@Parameter(description = "정책 이름") @PathVariable String policyName);

    @Operation(summary = "정책에 권한(Permission) 할당", description = "특정 정책에 권한을 할당합니다.")
    @ApiResponse(responseCode = "200", description = "할당 성공")
    @PostMapping("/{policyName}/permissions")
    ResponseEntity<Void> assignPermissionToPolicy(
        @Parameter(description = "정책 이름") @PathVariable String policyName,
        @RequestBody @Valid PermissionAssignRequest request
    );

    @Operation(summary = "정책에서 권한(Permission) 제거", description = "특정 정책에서 권한 할당을 해제합니다.")
    @ApiResponse(responseCode = "204", description = "제거 성공")
    @DeleteMapping("/{policyName}/permissions/{permissionName}")
    ResponseEntity<Void> removePermission(
        @Parameter(description = "정책 이름") @PathVariable String policyName,
        @Parameter(description = "권한 이름") @PathVariable String permissionName
    );
}
