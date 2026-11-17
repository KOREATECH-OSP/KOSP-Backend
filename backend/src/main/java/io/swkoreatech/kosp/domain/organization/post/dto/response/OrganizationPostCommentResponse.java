package io.swkoreatech.kosp.domain.organization.post.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostComment;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;

public record OrganizationPostCommentResponse(
    Long id,
    String content,
    AuthorResponse author,
    LocalDateTime createdAt
) {
    public static OrganizationPostCommentResponse from(OrganizationPostComment comment) {
        return new OrganizationPostCommentResponse(
            comment.getId(),
            comment.getContent(),
            AuthorResponse.from(comment.getAuthor()),
            comment.getCreatedAt()
        );
    }
}
