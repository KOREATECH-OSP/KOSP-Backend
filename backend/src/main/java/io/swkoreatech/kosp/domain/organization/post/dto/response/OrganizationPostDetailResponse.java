package io.swkoreatech.kosp.domain.organization.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPost;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostCategory;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostVisibility;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;

public record OrganizationPostDetailResponse(
    Long id,
    OrganizationPostCategory category,
    OrganizationPostVisibility visibility,
    String title,
    String description,
    String content,
    AuthorResponse author,
    int commentsCount,
    List<OrganizationPostFileResponse> files,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static OrganizationPostDetailResponse from(OrganizationPost post) {
        return new OrganizationPostDetailResponse(
            post.getId(),
            post.getCategory(),
            post.getVisibility(),
            post.getTitle(),
            post.getDescription(),
            post.getContent(),
            AuthorResponse.from(post.getAuthor()),
            post.getCommentsCount(),
            post.getFiles().stream().map(OrganizationPostFileResponse::from).toList(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}
