package io.swkoreatech.kosp.domain.community.recruit.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;
import lombok.Builder;

@Builder
public record RecruitResponse(
    Long id,
    Long boardId,
    String title,
    String content,
    AuthorResponse author,
    Integer views,
    Integer likes,
    Integer comments,
    List<String> tags,
    Long teamId,
    RecruitStatus status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Boolean isLiked,
    Boolean isBookmarked
) {
    public static RecruitResponse from(Recruit recruit, boolean isLiked, boolean isBookmarked) {
        return RecruitResponse.builder()
            .id(recruit.getId())
            .boardId(recruit.getBoardId())
            .title(recruit.getTitle())
            .content(recruit.getContent())
            .author(AuthorResponse.from(recruit.getAuthor()))
            .views(recruit.getViews())
            .likes(recruit.getLikes())
            .comments(recruit.getCommentsCount())
            .tags(new ArrayList<>(recruit.getTags()))
            .teamId(recruit.getTeam().getId())
            .status(recruit.getStatus())
            .startDate(recruit.getStartDate())
            .endDate(recruit.getEndDate())
            .isLiked(isLiked)
            .isBookmarked(isBookmarked)
            .build();
    }
}
