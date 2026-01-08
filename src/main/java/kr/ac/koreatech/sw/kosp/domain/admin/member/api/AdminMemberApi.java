package kr.ac.koreatech.sw.kosp.domain.admin.member.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.admin.member.dto.request.AdminUserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.member.dto.request.UserRoleUpdateRequest;

@Tag(name = "Admin - Member", description = "관리자 전용 사용자 관리 API")
@RequestMapping("/v1/admin/users")
public interface AdminMemberApi {

    @Operation(summary = "사용자 목록 조회", description = "관리자 권한으로 모든 사용자 목록을 페이지네이션하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @org.springframework.web.bind.annotation.GetMapping
    ResponseEntity<kr.ac.koreatech.sw.kosp.domain.admin.member.dto.response.AdminUserListResponse> getUsers(
        @Parameter(description = "페이지 번호 (0부터 시작)") @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "사용자 삭제 (강제 탈퇴)", description = "관리자 권한으로 사용자를 강제 탈퇴(Soft Delete) 처리합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> deleteUser(@PathVariable Long userId);


    @Operation(summary = "사용자 정보 수정 (관리자)", description = "관리자 권한으로 사용자의 정보를 강제로 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/{userId}")
    ResponseEntity<Void> updateUser(
        @Parameter(description = "사용자 ID") @PathVariable Long userId,
        @RequestBody @Valid AdminUserUpdateRequest request
    );

    @Operation(summary = "사용자 역할 변경", description = "관리자 권한으로 사용자의 역할을 변경합니다.")
    @ApiResponse(responseCode = "200", description = "변경 성공")
    @PutMapping("/{userId}/roles")
    ResponseEntity<Void> updateUserRoles(
        @Parameter(description = "사용자 ID") @PathVariable Long userId,
        @RequestBody @Valid UserRoleUpdateRequest request
    );
}
