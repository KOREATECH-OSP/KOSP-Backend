package io.swkoreatech.kosp.domain.organization.post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.organization.model.Organization;
import io.swkoreatech.kosp.common.organization.model.OrganizationMemberRole;
import io.swkoreatech.kosp.common.organization.repository.OrganizationMemberRepository;
import io.swkoreatech.kosp.common.organization.repository.OrganizationRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.post.dto.request.CreateOrganizationPostCommentRequest;
import io.swkoreatech.kosp.domain.organization.post.dto.request.CreateOrganizationPostRequest;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostCommentResponse;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostDetailResponse;
import io.swkoreatech.kosp.domain.organization.post.dto.response.OrganizationPostSummaryResponse;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPost;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostCategory;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostComment;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostVisibility;
import io.swkoreatech.kosp.domain.organization.post.repository.OrganizationPostCommentRepository;
import io.swkoreatech.kosp.domain.organization.post.repository.OrganizationPostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationPostService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationPostRepository postRepository;
    private final OrganizationPostCommentRepository commentRepository;

    public List<OrganizationPostSummaryResponse> getPosts(Long organizationId, OrganizationPostCategory category, User user) {
        Organization organization = organizationRepository.getById(organizationId);
        boolean isMember = isMember(organizationId, user);

        List<OrganizationPost> posts = category != null
            ? postRepository.findAllByOrganizationIdAndCategory(organization.getId(), category)
            : postRepository.findAllByOrganizationId(organization.getId());

        return posts.stream()
            .filter(post -> canRead(post, isMember))
            .map(OrganizationPostSummaryResponse::from)
            .toList();
    }

    public OrganizationPostDetailResponse getPost(Long organizationId, Long postId, User user) {
        boolean isMember = isMember(organizationId, user);
        OrganizationPost post = postRepository.findByIdAndOrganizationId(postId, organizationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_POST_NOT_FOUND));

        if (!canRead(post, isMember)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        return OrganizationPostDetailResponse.from(post);
    }

    @Transactional
    public OrganizationPostSummaryResponse createPost(Long organizationId, CreateOrganizationPostRequest request, User user) {
        requireMember(organizationId, user);
        Organization organization = organizationRepository.getById(organizationId);

        List<io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostFile> files =
            request.files() != null
                ? request.files().stream().map(CreateOrganizationPostRequest.FileDto::toEntity).toList()
                : List.of();

        OrganizationPost post = OrganizationPost.builder()
            .organization(organization)
            .author(user)
            .category(request.category())
            .visibility(request.visibility())
            .title(request.title())
            .description(request.description())
            .content(request.content())
            .files(files)
            .build();

        return OrganizationPostSummaryResponse.from(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long organizationId, Long postId, User user) {
        OrganizationPost post = postRepository.findByIdAndOrganizationId(postId, organizationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_POST_NOT_FOUND));

        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = isAdminOrOwner(organizationId, user);
        if (!isAuthor && !isAdmin) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        postRepository.delete(post);
    }

    public List<OrganizationPostCommentResponse> getComments(Long organizationId, Long postId, User user) {
        boolean isMember = isMember(organizationId, user);
        OrganizationPost post = postRepository.findByIdAndOrganizationId(postId, organizationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_POST_NOT_FOUND));
        if (!canRead(post, isMember)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        return commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId).stream()
            .map(OrganizationPostCommentResponse::from)
            .toList();
    }

    @Transactional
    public OrganizationPostCommentResponse createComment(Long organizationId, Long postId, CreateOrganizationPostCommentRequest request, User user) {
        requireMember(organizationId, user);
        OrganizationPost post = postRepository.findByIdAndOrganizationId(postId, organizationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_POST_NOT_FOUND));
        if (!canRead(post, true)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }

        OrganizationPostComment comment = OrganizationPostComment.builder()
            .post(post)
            .author(user)
            .content(request.content())
            .build();
        post.incrementCommentsCount();
        return OrganizationPostCommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long organizationId, Long postId, Long commentId, User user) {
        postRepository.findByIdAndOrganizationId(postId, organizationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_POST_NOT_FOUND));
        OrganizationPostComment comment = commentRepository.findByIdAndPostId(commentId, postId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_POST_COMMENT_NOT_FOUND));

        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isAdmin = isAdminOrOwner(organizationId, user);
        if (!isAuthor && !isAdmin) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        commentRepository.delete(comment);

        OrganizationPost post = comment.getPost();
        post.decrementCommentsCount();
    }

    private boolean canRead(OrganizationPost post, boolean isMember) {
        return post.getVisibility() == OrganizationPostVisibility.PUBLIC || isMember;
    }

    private boolean isMember(Long organizationId, User user) {
        if (user == null) {
            return false;
        }
        return organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, user.getId()).isPresent();
    }

    private void requireMember(Long organizationId, User user) {
        if (user == null || !isMember(organizationId, user)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

    private boolean isAdminOrOwner(Long organizationId, User user) {
        if (user == null) {
            return false;
        }
        return organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, user.getId())
            .map(m -> m.getRole() == OrganizationMemberRole.OWNER || m.getRole() == OrganizationMemberRole.ADMIN)
            .orElse(false);
    }
}
