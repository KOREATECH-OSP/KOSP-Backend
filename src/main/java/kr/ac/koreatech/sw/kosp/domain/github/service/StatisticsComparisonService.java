package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import kr.ac.koreatech.sw.kosp.domain.github.dto.StatisticsComparisonResponse;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsComparisonService {

    private final GithubUserStatisticsRepository userStatisticsRepository;

    public StatisticsComparisonResponse getComparison(String githubId) {
        log.info("Getting statistics comparison for user: {}", githubId);

        // 1. 전체 사용자 통계 조회
        List<GithubUserStatistics> allStats = userStatisticsRepository.findAll();

        if (allStats.isEmpty()) {
            throw new IllegalStateException("No statistics data available");
        }

        // 2. 평균 계산
        DoubleSummaryStatistics commitsStats = allStats.stream()
            .mapToDouble(GithubUserStatistics::getTotalCommits)
            .summaryStatistics();

        DoubleSummaryStatistics starsStats = allStats.stream()
            .mapToDouble(GithubUserStatistics::getTotalStarsReceived)
            .summaryStatistics();

        DoubleSummaryStatistics prsStats = allStats.stream()
            .mapToDouble(GithubUserStatistics::getTotalPrs)
            .summaryStatistics();

        DoubleSummaryStatistics issuesStats = allStats.stream()
            .mapToDouble(GithubUserStatistics::getTotalIssues)
            .summaryStatistics();

        DoubleSummaryStatistics linesStats = allStats.stream()
            .mapToDouble(GithubUserStatistics::getTotalLines)
            .summaryStatistics();

        DoubleSummaryStatistics scoreStats = allStats.stream()
            .mapToDouble(s -> s.getTotalScore().doubleValue())
            .summaryStatistics();

        // 3. 사용자 통계 조회
        GithubUserStatistics userStats = userStatisticsRepository
            .findByGithubId(githubId)
            .orElseThrow(() -> new EntityNotFoundException("User statistics not found: " + githubId));

        // 4. 순위 계산
        int commitsRank = calculateRank(allStats, userStats, 
            Comparator.comparing(GithubUserStatistics::getTotalCommits).reversed());
        
        int starsRank = calculateRank(allStats, userStats,
            Comparator.comparing(GithubUserStatistics::getTotalStarsReceived).reversed());
        
        int prsRank = calculateRank(allStats, userStats,
            Comparator.comparing(GithubUserStatistics::getTotalPrs).reversed());
        
        int issuesRank = calculateRank(allStats, userStats,
            Comparator.comparing(GithubUserStatistics::getTotalIssues).reversed());
        
        int linesRank = calculateRank(allStats, userStats,
            Comparator.comparing(GithubUserStatistics::getTotalLines).reversed());
        
        int scoreRank = calculateRank(allStats, userStats,
            Comparator.comparing(GithubUserStatistics::getTotalScore).reversed());

        // 5. 백분위 계산 (상위 몇 %)
        int totalUsers = allStats.size();
        double commitsPercentile = calculatePercentile(commitsRank, totalUsers);
        double starsPercentile = calculatePercentile(starsRank, totalUsers);
        double prsPercentile = calculatePercentile(prsRank, totalUsers);
        double issuesPercentile = calculatePercentile(issuesRank, totalUsers);
        double linesPercentile = calculatePercentile(linesRank, totalUsers);
        double scorePercentile = calculatePercentile(scoreRank, totalUsers);

        return StatisticsComparisonResponse.builder()
            // 평균
            .avgCommits(commitsStats.getAverage())
            .avgStars(starsStats.getAverage())
            .avgPrs(prsStats.getAverage())
            .avgIssues(issuesStats.getAverage())
            .avgLines(linesStats.getAverage())
            .avgScore(scoreStats.getAverage())
            // 나의 통계
            .myCommits(userStats.getTotalCommits())
            .myStars(userStats.getTotalStarsReceived())
            .myPrs(userStats.getTotalPrs())
            .myIssues(userStats.getTotalIssues())
            .myLines(userStats.getTotalLines())
            .myScore(userStats.getTotalScore().doubleValue())
            // 순위
            .commitsRank(commitsRank)
            .starsRank(starsRank)
            .prsRank(prsRank)
            .issuesRank(issuesRank)
            .linesRank(linesRank)
            .totalScoreRank(scoreRank)
            // 백분위
            .commitsPercentile(commitsPercentile)
            .starsPercentile(starsPercentile)
            .prsPercentile(prsPercentile)
            .issuesPercentile(issuesPercentile)
            .linesPercentile(linesPercentile)
            .totalScorePercentile(scorePercentile)
            // 전체 사용자 수
            .totalUsers(totalUsers)
            .build();
    }

    private int calculateRank(
        List<GithubUserStatistics> allStats,
        GithubUserStatistics userStats,
        Comparator<GithubUserStatistics> comparator
    ) {
        List<GithubUserStatistics> sorted = allStats.stream()
            .sorted(comparator)
            .toList();
        
        return sorted.indexOf(userStats) + 1;
    }

    private double calculatePercentile(int rank, int totalUsers) {
        // 상위 몇 % 계산
        return ((double) (totalUsers - rank + 1) / totalUsers) * 100.0;
    }
}
