package io.swkoreatech.kosp.domain.admin.role.api;

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
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyAssignRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.RoleRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.RoleUpdateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.response.RoleResponse;

@Tag(name = "Admin - Role", description = "관리자 전용 역할 관리 API")
@RequestMapping("/v1/admin/roles")
public interface AdminRoleApi {

    @Operation(summary = "모든 역할(Role) 조회", description = "시스템에 등록된 모든 역할을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<List<RoleResponse>> getAllRoles();

    @Operation(summary = "역할(Role) 단일 조회", description = "특정 역할의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{roleName}")
    ResponseEntity<RoleResponse> getRole(@Parameter(description = "역할 이름") @PathVariable String roleName);

    @Operation(summary = "새로운 역할(Role) 생성", description = "새로운 역할을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping
    ResponseEntity<Void> createRole(@RequestBody @Valid RoleRequest request);

    @Operation(summary = "역할(Role) 수정", description = "특정 역할의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/{roleName}")
    ResponseEntity<Void> updateRole(
        @Parameter(description = "역할 이름") @PathVariable String roleName,
        @RequestBody @Valid RoleUpdateRequest request
    );

    @Operation(summary = "역할(Role) 삭제", description = "특정 역할을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{roleName}")
    ResponseEntity<Void> deleteRole(@Parameter(description = "역할 이름") @PathVariable String roleName);

    @Operation(summary = "역할에 정책(Policy) 할당", description = "특정 역할에 정책을 할당합니다.")
    @ApiResponse(responseCode = "200", description = "할당 성공")
    @PostMapping("/{roleName}/policies")
    ResponseEntity<Void> assignPolicy(
        @Parameter(description = "역할 이름") @PathVariable String roleName,
        @RequestBody @Valid PolicyAssignRequest request
    );

    @Operation(summary = "역할에서 정책(Policy) 제거", description = "특정 역할에서 정책 할당을 해제합니다.")
    @ApiResponse(responseCode = "204", description = "제거 성공")
    @DeleteMapping("/{roleName}/policies/{policyName}")
    ResponseEntity<Void> removePolicy(
        @Parameter(description = "역할 이름") @PathVariable String roleName,
        @Parameter(description = "정책 이름") @PathVariable String policyName
    );
}
