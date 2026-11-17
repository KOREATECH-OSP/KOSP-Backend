package io.swkoreatech.kosp.domain.organization.post.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.post.api.OrganizationPostApi;
import io.swkoreatech.kosp.domain.organization.post.dto.request.CreateOrganizationPostCommentRequest;
import io.swkoreatech.kosp.domain.organization.post.dto.request.CreateOrganizationPostRequest;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostCommentResponse;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostDetailResponse;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostSummaryResponse;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostCategory;
import io.swkoreatech.kosp.domain.organization.post.service.OrganizationPostService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrganizationPostController implements OrganizationPostApi {

    private final OrganizationPostService postService;

    @Override
    @Permit(permitAll = true, name = "org:posts:list", description = "조직 게시글 목록 조회")
    public ResponseEntity<List<OrganizationPostSummaryResponse>> getPosts(
        Long organizationId, OrganizationPostCategory category, User user
    ) {
        return ResponseEntity.ok(postService.getPosts(organizationId, category, user));
    }

    @Override
    @Permit(permitAll = true, name = "org:posts:detail", description = "조직 게시글 상세 조회")
    public ResponseEntity<OrganizationPostDetailResponse> getPost(
        Long organizationId, Long postId, User user
    ) {
        return ResponseEntity.ok(postService.getPost(organizationId, postId, user));
    }

    @Override
    @Permit(name = "org:posts:create", description = "조직 게시글 작성 (멤버만)")
    public ResponseEntity<OrganizationPostSummaryResponse> createPost(
        Long organizationId, CreateOrganizationPostRequest request, User user
    ) {
        OrganizationPostSummaryResponse response = postService.createPost(organizationId, request, user);
        return ResponseEntity.created(
            URI.create("/v1/organizations/" + organizationId + "/posts/" + response.id())
        ).body(response);
    }

    @Override
    @Permit(name = "org:posts:delete", description = "조직 게시글 삭제")
    public ResponseEntity<Void> deletePost(Long organizationId, Long postId, User user) {
        postService.deletePost(organizationId, postId, user);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(permitAll = true, name = "org:posts:comments:list", description = "조직 댓글 목록 조회")
    public ResponseEntity<List<OrganizationPostCommentResponse>> getComments(
        Long organizationId, Long postId, User user
    ) {
        return ResponseEntity.ok(postService.getComments(organizationId, postId, user));
    }

    @Override
    @Permit(name = "org:posts:comments:create", description = "조직 댓글 작성 (멤버만)")
    public ResponseEntity<OrganizationPostCommentResponse> createComment(
        Long organizationId, Long postId, CreateOrganizationPostCommentRequest request, User user
    ) {
        return ResponseEntity.ok(postService.createComment(organizationId, postId, request, user));
    }

    @Override
    @Permit(name = "org:posts:comments:delete", description = "조직 댓글 삭제")
    public ResponseEntity<Void> deleteComment(
        Long organizationId, Long postId, Long commentId, User user
    ) {
        postService.deleteComment(organizationId, postId, commentId, user);
        return ResponseEntity.noContent().build();
    }
}
