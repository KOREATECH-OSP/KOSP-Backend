package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;
import lombok.Builder;

import kr.ac.koreatech.sw.kosp.domain.user.dto.response.AuthorResponse;

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
            .tags(new java.util.ArrayList<>(recruit.getTags()))
            .teamId(recruit.getTeamId())
            .status(recruit.getStatus())
            .startDate(recruit.getStartDate())
            .endDate(recruit.getEndDate())
            .isLiked(isLiked)
            .isBookmarked(isBookmarked)
            .build();
    }
}
