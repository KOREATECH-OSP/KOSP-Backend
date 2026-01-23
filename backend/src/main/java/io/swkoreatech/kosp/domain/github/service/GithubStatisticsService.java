package io.swkoreatech.kosp.domain.github.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionComparisonResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionScoreResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubOverallHistoryResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GlobalStatisticsResponse;
import io.swkoreatech.kosp.domain.github.model.PlatformStatistics;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.domain.github.repository.PlatformStatisticsRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GithubStatisticsService {

    private final UserRepository userRepository;
    private final GithubUserStatisticsRepository statisticsRepository;
    private final PlatformStatisticsRepository platformStatisticsRepository;

    public GithubOverallHistoryResponse getOverallHistory(Long userId) {
        GithubUserStatistics stats = getStatisticsByUserId(userId);
        return GithubOverallHistoryResponse.from(stats);
    }

    public GithubContributionComparisonResponse getComparison(Long userId) {
        GithubUserStatistics userStats = getStatisticsByUserId(userId);
        PlatformStatistics platformStats = platformStatisticsRepository.getGlobal();

        if (platformStats == null) {
            return new GithubContributionComparisonResponse(
                0.0, 0.0, 0.0, 0.0,
                userStats.getTotalCommits(),
                userStats.getTotalStarsReceived(),
                userStats.getTotalPrs(),
                userStats.getTotalIssues()
            );
        }

        return new GithubContributionComparisonResponse(
            platformStats.getAvgCommitCount().doubleValue(),
            platformStats.getAvgStarCount().doubleValue(),
            platformStats.getAvgPrCount().doubleValue(),
            platformStats.getAvgIssueCount().doubleValue(),
            userStats.getTotalCommits(),
            userStats.getTotalStarsReceived(),
            userStats.getTotalPrs(),
            userStats.getTotalIssues()
        );
    }

    public GithubContributionScoreResponse getScore(Long userId) {
        GithubUserStatistics stats = getStatisticsByUserId(userId);
        return GithubContributionScoreResponse.from(stats);
    }

    public GlobalStatisticsResponse getGlobalStatistics() {
        PlatformStatistics stats = platformStatisticsRepository.getGlobal();
        if (stats == null) {
            return GlobalStatisticsResponse.builder()
                .avgCommitCount(0.0)
                .avgStarCount(0.0)
                .avgPrCount(0.0)
                .avgIssueCount(0.0)
                .totalUsers(0)
                .calculatedAt(java.time.LocalDateTime.now())
                .build();
        }
        return GlobalStatisticsResponse.builder()
            .avgCommitCount(stats.getAvgCommitCount().doubleValue())
            .avgStarCount(stats.getAvgStarCount().doubleValue())
            .avgPrCount(stats.getAvgPrCount().doubleValue())
            .avgIssueCount(stats.getAvgIssueCount().doubleValue())
            .totalUsers(stats.getTotalUserCount())
            .calculatedAt(stats.getCalculatedAt())
            .build();
    }

    private GithubUserStatistics getStatisticsByUserId(Long userId) {
        User user = userRepository.getById(userId);

        if (user.getGithubUser() == null) {
            throw new GlobalException(ExceptionMessage.GITHUB_USER_NOT_FOUND);
        }

        String githubId = String.valueOf(user.getGithubUser().getGithubId());
        return statisticsRepository.findByGithubId(githubId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
