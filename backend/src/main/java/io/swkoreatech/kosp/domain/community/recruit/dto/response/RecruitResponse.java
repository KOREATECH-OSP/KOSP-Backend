package io.swkoreatech.kosp.domain.community.recruit.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;
import lombok.Builder;

/**
 * 모집 공고 응답 DTO.
 *
 * @param id 모집 공고 ID
 * @param boardId 게시판 ID
 * @param title 제목
 * @param content 내용
 * @param author 작성자 정보
 * @param views 조회수
 * @param likes 좋아요 수
 * @param comments 댓글 수
 * @param tags 태그 목록
 * @param teamId 팀 ID
 * @param status 모집 상태
 * @param startDate 모집 시작일
 * @param endDate 모집 종료일
 * @param isLiked 좋아요 여부
 * @param isBookmarked 북마크 여부
 * @param canApply 지원 가능 여부
 * @param isDeleted 삭제 여부
 */
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
    Boolean isBookmarked,
    Boolean canApply,
    Boolean isDeleted
) {
    /**
     * 모집 공고 엔티티로부터 응답 객체를 생성한다.
     *
     * @param recruit 모집 공고 엔티티
     * @param isLiked 좋아요 여부
     * @param isBookmarked 북마크 여부
     * @param canApply 지원 가능 여부
     * @return 모집 공고 응답
     */
    public static RecruitResponse from(Recruit recruit, boolean isLiked, boolean isBookmarked, boolean canApply) {
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
            .canApply(canApply)
            .isDeleted(recruit.isDeleted())
            .build();
    }
}
