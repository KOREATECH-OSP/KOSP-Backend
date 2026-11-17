package io.swkoreatech.kosp.domain.organization.post.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPost;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostCategory;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostVisibility;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;

public record OrganizationPostSummaryResponse(
    Long id,
    OrganizationPostCategory category,
    OrganizationPostVisibility visibility,
    String title,
    String description,
    AuthorResponse author,
    int commentsCount,
    LocalDateTime createdAt
) {
    public static OrganizationPostSummaryResponse from(OrganizationPost post) {
        return new OrganizationPostSummaryResponse(
            post.getId(),
            post.getCategory(),
            post.getVisibility(),
            post.getTitle(),
            post.getDescription(),
            AuthorResponse.from(post.getAuthor()),
            post.getCommentsCount(),
            post.getCreatedAt()
        );
    }
}
