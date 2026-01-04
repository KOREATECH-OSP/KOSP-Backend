package kr.ac.koreatech.sw.kosp.domain.admin.role.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PermissionAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PermissionUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PermissionResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PolicyResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.RoleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Admin - Role", description = "관리자 전용 역할 및 정책 관리 API")
@RequestMapping("/v1/admin")
public interface AdminRoleApi {

    @Operation(summary = "모든 역할(Role) 조회", description = "시스템에 등록된 모든 역할을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/roles")
    ResponseEntity<List<RoleResponse>> getAllRoles();

    @Operation(summary = "역할(Role) 단일 조회", description = "특정 역할의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/roles/{roleName}")
    ResponseEntity<RoleResponse> getRole(@Parameter(description = "역할 이름") @PathVariable String roleName);

    @Operation(summary = "새로운 역할(Role) 생성", description = "새로운 역할을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping("/roles")
    ResponseEntity<Void> createRole(@RequestBody @Valid RoleRequest request);

    @Operation(summary = "역할(Role) 수정", description = "특정 역할의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/roles/{roleName}")
    ResponseEntity<Void> updateRole(
        @Parameter(description = "역할 이름") @PathVariable String roleName,
        @RequestBody @Valid RoleUpdateRequest request
    );

    @Operation(summary = "역할(Role) 삭제", description = "특정 역할을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/roles/{roleName}")
    ResponseEntity<Void> deleteRole(@Parameter(description = "역할 이름") @PathVariable String roleName);

    @Operation(summary = "역할에 정책(Policy) 할당", description = "특정 역할에 정책을 할당합니다.")
    @ApiResponse(responseCode = "200", description = "할당 성공")
    @PostMapping("/roles/{roleName}/policies")
    ResponseEntity<Void> assignPolicy(
        @Parameter(description = "역할 이름") @PathVariable String roleName,
        @RequestBody @Valid PolicyAssignRequest request
    );

    @Operation(summary = "역할에서 정책(Policy) 제거", description = "특정 역할에서 정책 할당을 해제합니다.")
    @ApiResponse(responseCode = "204", description = "제거 성공")
    @DeleteMapping("/roles/{roleName}/policies/{policyName}")
    ResponseEntity<Void> removePolicy(
        @Parameter(description = "역할 이름") @PathVariable String roleName,
        @Parameter(description = "정책 이름") @PathVariable String policyName
    );

    @Operation(summary = "정책 목록 조회", description = "시스템에 등록된 모든 정책을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/policies")
    ResponseEntity<List<PolicyResponse>> getAllPolicies();

    @Operation(summary = "정책 단일 조회", description = "특정 정책의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/policies/{policyName}")
    ResponseEntity<PolicyResponse> getPolicy(@Parameter(description = "정책 이름") @PathVariable String policyName);

    @Operation(summary = "정책 생성", description = "새로운 정책을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping("/policies")
    ResponseEntity<Void> createPolicy(@RequestBody @Valid PolicyCreateRequest request);

    @Operation(summary = "정책 수정", description = "특정 정책의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/policies/{policyName}")
    ResponseEntity<Void> updatePolicy(
        @Parameter(description = "정책 이름") @PathVariable String policyName,
        @RequestBody @Valid PolicyUpdateRequest request
    );

    @Operation(summary = "정책 삭제", description = "특정 정책을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/policies/{policyName}")
    ResponseEntity<Void> deletePolicy(@Parameter(description = "정책 이름") @PathVariable String policyName);

    @Operation(summary = "정책에 권한(Permission) 할당", description = "특정 정책에 권한을 할당합니다.")
    @ApiResponse(responseCode = "200", description = "할당 성공")
    @PostMapping("/policies/{policyName}/permissions")
    ResponseEntity<Void> assignPermissionToPolicy(
        @Parameter(description = "정책 이름") @PathVariable String policyName,
        @RequestBody @Valid PermissionAssignRequest request
    );

    @Operation(summary = "정책에서 권한(Permission) 제거", description = "특정 정책에서 권한 할당을 해제합니다.")
    @ApiResponse(responseCode = "204", description = "제거 성공")
    @DeleteMapping("/policies/{policyName}/permissions/{permissionName}")
    ResponseEntity<Void> removePermission(
        @Parameter(description = "정책 이름") @PathVariable String policyName,
        @Parameter(description = "권한 이름") @PathVariable String permissionName
    );

    @Operation(summary = "권한 목록 조회", description = "시스템에 등록된 모든 권한(Permission)을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/permissions")
    ResponseEntity<List<PermissionResponse>> getAllPermissions();

    @Operation(summary = "권한 단일 조회", description = "특정 권한의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/permissions/{permissionName}")
    ResponseEntity<PermissionResponse> getPermission(@Parameter(description = "권한 이름") @PathVariable String permissionName);

    @Operation(summary = "권한 수정", description = "특정 권한의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/permissions/{permissionName}")
    ResponseEntity<Void> updatePermission(
        @Parameter(description = "권한 이름") @PathVariable String permissionName,
        @RequestBody @Valid PermissionUpdateRequest request
    );

    @Operation(summary = "권한 삭제", description = "특정 권한을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/permissions/{permissionName}")
    ResponseEntity<Void> deletePermission(@Parameter(description = "권한 이름") @PathVariable String permissionName);
}
