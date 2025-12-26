package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecruitResponse {
    private final Long id;
    private final Long boardId;
    private final String title;
    private final String content;
    private final Integer authorId;
    private final Integer views;
    private final Integer likes;
    private final Integer comments;
    private final List<String> tags;
    private final Long teamId;
    private final RecruitmentStatus status;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public static RecruitResponse from(Recruitment recruit) {
    Long authorId,
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
