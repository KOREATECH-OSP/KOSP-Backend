package kr.ac.koreatech.sw.kosp.domain.github.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ActivityTimelineResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ContributionOverviewResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ContributionPatternResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubMonthlyActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.LanguageDistributionResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.RepositoryStatsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.YearlyAnalysisResponse;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/users/{userId}/github")
@RequiredArgsConstructor
public class GithubStatisticsController {

    private final GithubStatisticsService statisticsService;

    /**
     * 전체 기여 내역 요약 조회
     */
    @GetMapping("/contribution-overview")
    public ResponseEntity<ContributionOverviewResponse> getContributionOverview(
        @PathVariable Long userId
    ) {
        ContributionOverviewResponse response = statisticsService.getContributionOverview(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 기여 성향 분석 조회
     */
    @GetMapping("/contribution-pattern")
    public ResponseEntity<ContributionPatternResponse> getContributionPattern(
        @PathVariable Long userId
    ) {
        ContributionPatternResponse response = statisticsService.getContributionPattern(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 연도별 분석 조회
     */
    @GetMapping("/yearly-analysis")
    public ResponseEntity<YearlyAnalysisResponse> getYearlyAnalysis(
        @PathVariable Long userId,
        @RequestParam int year
    ) {
        YearlyAnalysisResponse response = statisticsService.getYearlyAnalysis(userId, year);
        return ResponseEntity.ok(response);
    }

    /**
     * 저장소별 통계 조회
     */
    @GetMapping("/repository-stats")
    public ResponseEntity<RepositoryStatsResponse> getRepositoryStats(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "commits") String sortBy
    ) {
        RepositoryStatsResponse response = statisticsService.getRepositoryStats(userId, limit, sortBy);
        return ResponseEntity.ok(response);
    }

    /**
     * 언어 분포 조회
     */
    @GetMapping("/language-distribution")
    public ResponseEntity<LanguageDistributionResponse> getLanguageDistribution(
        @PathVariable Long userId
    ) {
        LanguageDistributionResponse response = statisticsService.getLanguageDistribution(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 월별 활동 조회
     */
    @GetMapping("/monthly-activity")
    public ResponseEntity<GithubMonthlyActivityResponse> getMonthlyActivity(
        @PathVariable Long userId,
        @RequestParam(required = false) Integer startYear,
        @RequestParam(required = false) Integer startMonth,
        @RequestParam(required = false) Integer endYear,
        @RequestParam(required = false) Integer endMonth
    ) {
        GithubMonthlyActivityResponse response = statisticsService.getMonthlyActivity(
            userId, startYear, startMonth, endYear, endMonth
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 활동 타임라인 조회
     */
    @GetMapping("/activity-timeline")
    public ResponseEntity<ActivityTimelineResponse> getActivityTimeline(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "90") int days
    ) {
        ActivityTimelineResponse response = statisticsService.getActivityTimeline(userId, limit, days);
        return ResponseEntity.ok(response);
    }
}
