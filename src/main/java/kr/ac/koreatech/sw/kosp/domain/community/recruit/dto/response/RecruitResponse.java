package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;
import lombok.Builder;

@Builder
public record RecruitResponse(
    Long id,
    Long boardId,
    String title,
    String content,
    Long authorId,
    Integer views,
    Integer likes,
    Integer comments,
    List<String> tags,
    Long teamId,
    RecruitStatus status,
    LocalDateTime startDate,
    LocalDateTime endDate
) {
    public static RecruitResponse from(Recruit recruit) {
        return RecruitResponse.builder()
            .id(recruit.getId())
            .boardId(recruit.getBoardId())
            .title(recruit.getTitle())
            .content(recruit.getContent())
            .authorId(recruit.getAuthorId())
            .views(recruit.getViews())
            .likes(recruit.getLikes())
            .comments(recruit.getCommentsCount())
            .tags(recruit.getTags())
            .teamId(recruit.getTeamId())
            .status(recruit.getStatus())
            .startDate(recruit.getStartDate())
            .endDate(recruit.getEndDate())
            .build();
    }
}
