package io.swkoreatech.kosp.domain.github.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.common.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionComparisonResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionScoreResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubOverallHistoryResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubRecentActivityResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubResumeProjectResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GlobalStatisticsResponse;
import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;
import io.swkoreatech.kosp.domain.github.model.PlatformStatistics;
import io.swkoreatech.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import io.swkoreatech.kosp.domain.github.repository.PlatformStatisticsRepository;
import lombok.RequiredArgsConstructor;

/**
 * GitHub 통계 서비스.
 * GitHub 기여 활동, 비교 통계, 점수 조회 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GithubStatisticsService {

    private static final Integer RECENT_ACTIVITY_LIMIT = 10;

    private final UserRepository userRepository;
    private final GithubUserStatisticsRepository statisticsRepository;
    private final GithubRepositoryStatisticsRepository repositoryStatisticsRepository;
    private final PlatformStatisticsRepository platformStatisticsRepository;

    /**
     * 사용자의 전체 기여 내역을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 전체 기여 내역 응답
     */
    public GithubOverallHistoryResponse getOverallHistory(Long userId) {
        GithubUserStatistics stats = getStatisticsByUserId(userId);
        return GithubOverallHistoryResponse.from(stats);
    }

    /**
     * 사용자와 전체 평균의 기여 내역을 비교 조회한다.
     *
     * @param userId 사용자 ID
     * @return 기여 내역 비교 응답
     */
    public GithubContributionComparisonResponse getComparison(Long userId) {
        GithubUserStatistics userStats = getStatisticsByUserId(userId);
        PlatformStatistics platformStats = platformStatisticsRepository.getGlobal();

        if (platformStats == null) {
            return GithubContributionComparisonResponse.empty(
                userStats.getTotalCommits(),
                userStats.getTotalStarsReceived(),
                userStats.getTotalPrs(),
                userStats.getTotalIssues()
            );
        }

        return GithubContributionComparisonResponse.from(
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

    /**
     * 사용자의 GitHub 기여 점수를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 기여 점수 응답
     */
    public GithubContributionScoreResponse getScore(Long userId) {
        GithubUserStatistics stats = getStatisticsByUserId(userId);
        return GithubContributionScoreResponse.from(stats);
    }

    /**
     * 사용자의 최근 기여 활동을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 최근 기여 활동 목록
     */
    public List<GithubRecentActivityResponse> getRecentActivity(Long userId) {
        User user = userRepository.getById(userId);

        if (user.getGithubUser() == null) {
            throw new GlobalException(ExceptionMessage.GITHUB_USER_NOT_FOUND);
        }

        String githubId = String.valueOf(user.getGithubUser().getGithubId());
        List<GithubRepositoryStatistics> repositories = repositoryStatisticsRepository
            .findTopNByContributorGithubIdOrderByLastCommitDateDesc(githubId, RECENT_ACTIVITY_LIMIT);
        return repositories.stream().map(GithubRecentActivityResponse::from).toList();
    }

    /**
     * 전체 사용자의 평균 기여 통계를 조회한다.
     *
     * @return 전체 통계 응답
     */
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

    /**
     * 로그인 사용자의 저장소 목록을 이력서 프로젝트 가져오기용으로 조회한다.
     * 소유 저장소 우선, 스타 수 내림차순, 최근 커밋 내림차순으로 정렬한다.
     *
     * @param user 로그인 사용자
     * @return 이력서 프로젝트 가져오기 항목 목록 (GitHub 미연동 시 빈 목록)
     */
    public List<GithubResumeProjectResponse> getMyRepositoriesForResume(User user) {
        if (user.getGithubUser() == null) {
            return List.of();
        }

        String githubId = String.valueOf(user.getGithubUser().getGithubId());
        return repositoryStatisticsRepository.findByContributorGithubId(githubId).stream()
            .sorted(Comparator
                .comparing((GithubRepositoryStatistics r) -> Boolean.TRUE.equals(r.getIsOwned()))
                .thenComparing(r -> nullSafeInt(r.getStargazersCount()))
                .thenComparing(GithubStatisticsService::lastCommitEpoch)
                .reversed())
            .map(GithubResumeProjectResponse::from)
            .toList();
    }

    private static int nullSafeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static long lastCommitEpoch(GithubRepositoryStatistics repo) {
        return repo.getLastCommitDate() == null
            ? Long.MIN_VALUE
            : repo.getLastCommitDate().toEpochSecond(java.time.ZoneOffset.UTC);
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
