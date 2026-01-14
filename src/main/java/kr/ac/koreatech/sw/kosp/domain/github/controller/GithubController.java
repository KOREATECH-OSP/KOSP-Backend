package kr.ac.koreatech.sw.kosp.domain.github.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.github.api.GithubApi;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ActivityTimelineResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ContributionOverviewResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ContributionPatternResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubAnalysisResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubMonthlyActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubRecentContributionsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubSummaryResponse;
// import kr.ac.koreatech.sw.kosp.domain.github.dto.response.LanguageDistributionResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GlobalStatisticsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.RepositoryStatsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.YearlyAnalysisResponse;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubService;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GithubController implements GithubApi {

    private final GithubService githubService;
    private final GithubStatisticsService githubStatisticsService;

    @Override
    public ResponseEntity<GithubAnalysisResponse> getGithubAnalysis(Long userId) {
        return ResponseEntity.ok(githubService.getAnalysis(userId));
    }

    @Override
    public ResponseEntity<GithubSummaryResponse> getSummary(Long userId) {
        return ResponseEntity.ok(githubStatisticsService.getSummary(userId));
    }

    @Override
    public ResponseEntity<GithubMonthlyActivityResponse> getMonthlyActivity(
        Long userId,
        Integer startYear,
        Integer startMonth,
        Integer endYear,
        Integer endMonth
    ) {
        return ResponseEntity.ok(
            githubStatisticsService.getMonthlyActivity(userId, startYear, startMonth, endYear, endMonth)
        );
    }

    @Override
    public ResponseEntity<GithubRecentContributionsResponse> getRecentContributions(
        Long userId,
        Integer limit
    ) {
        return ResponseEntity.ok(githubStatisticsService.getRecentContributions(userId, limit));
    }

    @Override
    public ResponseEntity<ContributionOverviewResponse> getContributionOverview(Long userId) {
        return ResponseEntity.ok(githubStatisticsService.getContributionOverview(userId));
    }

    @Override
    public ResponseEntity<ContributionPatternResponse> getContributionPattern(Long userId) {
        return ResponseEntity.ok(githubStatisticsService.getContributionPattern(userId));
    }

    @Override
    public ResponseEntity<YearlyAnalysisResponse> getYearlyAnalysis(Long userId, int year) {
        return ResponseEntity.ok(githubStatisticsService.getYearlyAnalysis(userId, year));
    }

    @Override
    public ResponseEntity<RepositoryStatsResponse> getRepositoryStats(Long userId, int limit, String sortBy) {
        return ResponseEntity.ok(githubStatisticsService.getRepositoryStats(userId, limit, sortBy));
    }

    @Override
    public ResponseEntity<Void> getLanguageDistribution(Long userId) {
        // return ResponseEntity.ok(githubStatisticsService.getLanguageDistribution(userId));
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ActivityTimelineResponse> getActivityTimeline(Long userId, int limit, int days) {
        return ResponseEntity.ok(githubStatisticsService.getActivityTimeline(userId, limit, days));
    }

    @Override
    public ResponseEntity<GlobalStatisticsResponse> getGlobalStatistics(Long userId) {
        return ResponseEntity.ok(githubStatisticsService.getGlobalStatistics());
    }
}
