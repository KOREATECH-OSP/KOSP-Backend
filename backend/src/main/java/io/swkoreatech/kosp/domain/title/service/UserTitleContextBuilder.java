package io.swkoreatech.kosp.domain.title.service;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.common.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.common.title.model.UserLoginStreak;
import io.swkoreatech.kosp.common.title.repository.UserLoginStreakRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 칭호 조건 평가에 필요한 {@link UserTitleContext}를 구성한다.
 *
 * <p>유저별로 GitHub 통계, 로그인 streak, 챌린지 달성 수 등 여러 소스에서
 * 데이터를 수집해 하나의 컨텍스트 객체로 조립한다.</p>
 *
 * <p>새 조건 유형 추가 시 여기에 데이터 소스를 추가하고
 * {@link UserTitleContext}에 필드를 추가한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserTitleContextBuilder {

    private final GithubUserStatisticsRepository githubUserStatisticsRepository;
    private final UserLoginStreakRepository userLoginStreakRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final ArticleRepository articleRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * 유저의 칭호 평가 컨텍스트를 구성한다.
     *
     * @param user 평가 대상 유저
     * @return 집계된 컨텍스트
     */
    @Transactional(readOnly = true)
    public UserTitleContext build(User user) {
        int commitCount = resolveCommitCount(user);
        int loginStreakDays = resolveLoginStreakDays(user);
        int completedChallengeCount = (int) challengeHistoryRepository.countByUserAndIsAchievedTrue(user);
        long articleCount = articleRepository.countByAuthorAndIsDeletedFalse(user);
        long teamJoinCount = teamMemberRepository.countByUserAndIsDeletedFalse(user);

        log.debug(
            "Built UserTitleContext for userId={}: commits={}, streak={}, challenges={}, articles={}, teams={}",
            user.getId(), commitCount, loginStreakDays, completedChallengeCount, articleCount, teamJoinCount
        );

        return UserTitleContext.builder()
            .userId(user.getId())
            .commitCount(commitCount)
            .loginStreakDays(loginStreakDays)
            .completedChallengeCount(completedChallengeCount)
            .articleCount(articleCount)
            .teamJoinCount(teamJoinCount)
            .build();
    }

    private int resolveCommitCount(User user) {
        if (user.getGithubUser() == null) {
            return 0;
        }
        String githubId = String.valueOf(user.getGithubUser().getGithubId());
        return githubUserStatisticsRepository.findByGithubId(githubId)
            .map(stats -> stats.getTotalCommits() != null ? stats.getTotalCommits() : 0)
            .orElse(0);
    }

    private int resolveLoginStreakDays(User user) {
        return userLoginStreakRepository.findByUser(user)
            .map(UserLoginStreak::getCurrentStreak)
            .orElse(0);
    }
}
