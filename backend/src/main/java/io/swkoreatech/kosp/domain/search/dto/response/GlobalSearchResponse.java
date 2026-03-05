package io.swkoreatech.kosp.domain.search.dto.response;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.global.dto.PageMeta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통합 검색 응답 DTO.
 *
 * @param articles 게시글 검색 결과
 * @param recruits 모집글 검색 결과
 * @param teams 팀 검색 결과
 * @param challenges 챌린지 검색 결과
 * @param users 사용자 검색 결과
 * @param repositories 저장소 검색 결과
 * @param meta 페이지 메타 정보
 */
public record GlobalSearchResponse(
    List<ArticleSummary> articles,
    List<RecruitSummary> recruits,
    List<TeamSummary> teams,
    List<ChallengeSummary> challenges,
    List<UserSummary> users,
    List<RepositorySummary> repositories,
    PageMeta meta
) {
    /** 각 카테고리별 검색 결과로부터 통합 검색 응답을 생성한다. */
    public static GlobalSearchResponse from(
        List<ArticleSummary> articles,
        List<RecruitSummary> recruits,
        List<TeamSummary> teams,
        List<ChallengeSummary> challenges,
        List<UserSummary> users,
        List<RepositorySummary> repositories,
        PageMeta meta
    ) {
        return new GlobalSearchResponse(articles, recruits, teams, challenges, users, repositories, meta);
    }

    /**
     * 게시글 요약 정보.
     *
     * @param id 게시글 ID
     * @param title 제목
     * @param authorName 작성자 이름
     * @param createdAt 생성일시
     */
    public record ArticleSummary(
        Long id,
        String title,
        String authorName,
        LocalDateTime createdAt
    ) {
        public static ArticleSummary from(Article article) {
            return new ArticleSummary(
                article.getId(),
                article.getTitle(),
                article.getAuthor().getName(),
                article.getCreatedAt()
            );
        }
    }

    /**
     * 모집글 요약 정보.
     *
     * @param id 모집글 ID
     * @param title 제목
     * @param authorName 작성자 이름
     * @param createdAt 생성일시
     * @param endDate 마감일시
     */
    public record RecruitSummary(
        Long id,
        String title,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime endDate
    ) {
        public static RecruitSummary from(Recruit recruit) {
            return new RecruitSummary(
                recruit.getId(),
                recruit.getTitle(),
                recruit.getAuthor().getName(),
                recruit.getCreatedAt(),
                recruit.getEndDate()
            );
        }
    }

    /**
     * 팀 요약 정보.
     *
     * @param id 팀 ID
     * @param name 팀 이름
     * @param description 팀 설명
     * @param memberCount 멤버 수
     */
    public record TeamSummary(
        Long id,
        String name,
        String description,
        Integer memberCount
    ) {
        public static TeamSummary from(Team team) {
            return new TeamSummary(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getMembers().size()
            );
        }
    }

    /**
     * 챌린지 요약 정보.
     *
     * @param id 챌린지 ID
     * @param name 챌린지 이름
     * @param description 챌린지 설명
     * @param tier 챌린지 티어
     */
    public record ChallengeSummary(
        Long id,
        String name,
        String description,
        Integer tier
    ) {
        public static ChallengeSummary from(Challenge challenge) {
            return new ChallengeSummary(
                challenge.getId(),
                challenge.getName(),
                challenge.getDescription(),
                challenge.getTier()
            );
        }
    }
}
