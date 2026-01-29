package io.swkoreatech.kosp.domain.search.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swkoreatech.kosp.domain.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.global.dto.PageMeta;

public record GlobalSearchResponse(
    List<ArticleSummary> articles,
    List<RecruitSummary> recruits,
    List<TeamSummary> teams,
    List<ChallengeSummary> challenges,
    List<UserSummary> users,
    PageMeta meta
) {
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
