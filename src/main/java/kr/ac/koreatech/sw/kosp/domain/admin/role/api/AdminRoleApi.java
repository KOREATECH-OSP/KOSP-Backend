package kr.ac.koreatech.sw.kosp.domain.admin.role.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PermissionResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PolicyResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.RoleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Admin - Role", description = "관리자 전용 역할 및 정책 관리 API")
@RequestMapping("/v1/admin")
public interface AdminRoleApi {

    @Operation(summary = "모든 역할(Role) 조회", description = "시스템에 등록된 모든 역할을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/roles")
    ResponseEntity<List<RoleResponse>> getAllRoles();

    @Operation(summary = "새로운 역할(Role) 생성", description = "새로운 역할을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping("/roles")
    ResponseEntity<Void> createRole(@RequestBody @Valid RoleRequest request);

    @Operation(summary = "역할에 정책(Policy) 할당", description = "특정 역할에 정책을 할당합니다.")
    @ApiResponse(responseCode = "200", description = "할당 성공")
    @PostMapping("/roles/{roleName}/policies")
    ResponseEntity<Void> assignPolicy(
        @Parameter(description = "역할 이름") @PathVariable String roleName,
        @RequestBody @Valid PolicyAssignRequest request
    );

    @Operation(summary = "정책 목록 조회", description = "시스템에 등록된 모든 정책을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/policies")
    ResponseEntity<List<PolicyResponse>> getAllPolicies();

    @Operation(summary = "정책 생성", description = "새로운 정책을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping("/policies")
    ResponseEntity<Void> createPolicy(@RequestBody @Valid PolicyCreateRequest request);

    @Operation(summary = "권한 목록 조회", description = "시스템에 등록된 모든 권한(Permission)을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/permissions")
    ResponseEntity<List<PermissionResponse>> getAllPermissions();
}
