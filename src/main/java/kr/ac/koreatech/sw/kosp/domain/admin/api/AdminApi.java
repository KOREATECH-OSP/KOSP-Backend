package kr.ac.koreatech.sw.kosp.domain.admin.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.PolicyAssignRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.UserRoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.response.RoleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;

@Tag(name = "Admin", description = "관리자 전용 API")
@RequestMapping("/v1/admin")
public interface AdminApi {

    @Operation(summary = "사용자 삭제 (강제 탈퇴)", description = "관리자 권한으로 사용자를 강제 탈퇴(Soft Delete) 처리합니다.")
    @DeleteMapping("/users/{userId}")
    ResponseEntity<Void> deleteUser(@PathVariable Long userId);

    @Operation(summary = "게시글 삭제", description = "관리자 권한으로 게시글을 삭제(Soft Delete)합니다.")
    @DeleteMapping("/articles/{articleId}")
    ResponseEntity<Void> deleteArticle(@PathVariable Long articleId);

    @Operation(summary = "공지사항 삭제", description = "관리자 권한으로 공지사항을 삭제합니다. (게시글 삭제와 동일 로직)")
    @DeleteMapping("/notices/{noticeId}")
    ResponseEntity<Void> deleteNotice(@PathVariable Long noticeId);

    @Operation(summary = "모든 역할(Role) 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/roles")
    ResponseEntity<List<RoleResponse>> getAllRoles();

    @Operation(summary = "새로운 역할(Role) 생성")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping("/roles")
    ResponseEntity<Void> createRole(@RequestBody @Valid RoleRequest request);

    @Operation(summary = "역할에 정책(Policy) 할당")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/roles/{roleName}/policies")
    ResponseEntity<Void> assignPolicy(
        @Parameter(description = "역할 이름") @PathVariable String roleName,
        @RequestBody @Valid PolicyAssignRequest request
    );

    @Operation(summary = "사용자 역할 변경")
    @ApiResponse(responseCode = "200", description = "성공")
    @PutMapping("/users/{userId}/roles")
    ResponseEntity<Void> updateUserRoles(
        @Parameter(description = "사용자 ID") @PathVariable Long userId,
        @RequestBody @Valid UserRoleUpdateRequest request
    );
}
