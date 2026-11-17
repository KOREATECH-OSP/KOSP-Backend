package io.swkoreatech.kosp.domain.organization.post.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.post.dto.request.CreateOrganizationPostCommentRequest;
import io.swkoreatech.kosp.domain.organization.post.dto.request.CreateOrganizationPostRequest;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostCommentResponse;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostDetailResponse;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostSummaryResponse;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostCategory;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

@Tag(name = "OrganizationPost", description = "조직 게시판 API")
@RequestMapping("/v1/organizations/{organizationId}/posts")
public interface OrganizationPostApi {

    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    ResponseEntity<List<OrganizationPostSummaryResponse>> getPosts(
        @PathVariable Long organizationId,
        @RequestParam(required = false) OrganizationPostCategory category,
        @AuthUser User user
    );

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    ResponseEntity<OrganizationPostDetailResponse> getPost(
        @PathVariable Long organizationId,
        @PathVariable Long postId,
        @AuthUser User user
    );

    @Operation(summary = "게시글 작성 (멤버만)")
    @PostMapping
    ResponseEntity<OrganizationPostSummaryResponse> createPost(
        @PathVariable Long organizationId,
        @RequestBody @Valid CreateOrganizationPostRequest request,
        @AuthUser User user
    );

    @Operation(summary = "게시글 삭제 (작성자 또는 관리자)")
    @DeleteMapping("/{postId}")
    ResponseEntity<Void> deletePost(
        @PathVariable Long organizationId,
        @PathVariable Long postId,
        @AuthUser User user
    );

    @Operation(summary = "댓글 목록 조회")
    @GetMapping("/{postId}/comments")
    ResponseEntity<List<OrganizationPostCommentResponse>> getComments(
        @PathVariable Long organizationId,
        @PathVariable Long postId,
        @AuthUser User user
    );

    @Operation(summary = "댓글 작성 (멤버만)")
    @PostMapping("/{postId}/comments")
    ResponseEntity<OrganizationPostCommentResponse> createComment(
        @PathVariable Long organizationId,
        @PathVariable Long postId,
        @RequestBody @Valid CreateOrganizationPostCommentRequest request,
        @AuthUser User user
    );

    @Operation(summary = "댓글 삭제 (작성자 또는 관리자)")
    @DeleteMapping("/{postId}/comments/{commentId}")
    ResponseEntity<Void> deleteComment(
        @PathVariable Long organizationId,
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @AuthUser User user
    );
}
