package kr.ac.koreatech.sw.kosp.domain.github.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ActivityTimelineResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ContributionOverviewResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ContributionPatternResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubAnalysisResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubContributionComparisonResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubContributionScoreResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubMonthlyActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubOverallHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubRecentActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubRecentContributionsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubSummaryResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GlobalStatisticsResponse;
// import kr.ac.koreatech.sw.kosp.domain.github.dto.response.LanguageDistributionResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.RepositoryStatsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.YearlyAnalysisResponse;
import java.util.List;

@Tag(name = "GitHub", description = "GitHub 관련 API")
@RequestMapping("/v1/users/{userId}/github")
public interface GithubApi {

/*
    @Operation(summary = "GitHub 활동 분석 조회", description = "사용자의 GitHub 활동 분석 결과(활동 시간대, 협업 성향 등)를 조회합니다.")
    @GetMapping("/analysis")
    ResponseEntity<GithubAnalysisResponse> getGithubAnalysis(
        @Parameter(description = "사용자 ID", required = true) 
        @PathVariable Long userId
    );

    @Operation(summary = "GitHub 통계 요약 조회", description = "사용자의 전체 GitHub 통계 요약을 조회합니다.")
    @GetMapping("/summary")
    ResponseEntity<GithubSummaryResponse> getSummary(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "월별 활동 추이 조회", description = "사용자의 월별 GitHub 활동 추이를 조회합니다.")
    @GetMapping("/monthly-activity")
    ResponseEntity<GithubMonthlyActivityResponse> getMonthlyActivity(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId,
        @Parameter(description = "시작 년도")
        @RequestParam(required = false) Integer startYear,
        @Parameter(description = "시작 월")
        @RequestParam(required = false) Integer startMonth,
        @Parameter(description = "종료 년도")
        @RequestParam(required = false) Integer endYear,
        @Parameter(description = "종료 월")
        @RequestParam(required = false) Integer endMonth
    );

    @Operation(summary = "최근 기여 활동 조회", description = "사용자의 최근 기여 활동을 조회합니다.")
    @GetMapping("/recent-contributions")
    ResponseEntity<GithubRecentContributionsResponse> getRecentContributions(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId,
        @Parameter(description = "조회할 저장소 개수", example = "10")
        @RequestParam(defaultValue = "10") Integer limit
    );

    @Operation(summary = "전체 기여 내역 요약 조회", description = "사용자의 전체 기여 내역 요약을 조회합니다.")
    @GetMapping("/contribution-overview")
    ResponseEntity<ContributionOverviewResponse> getContributionOverview(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "기여 성향 분석 조회", description = "사용자의 기여 성향(시간대, 프로젝트 패턴, 협업)을 분석합니다.")
    @GetMapping("/contribution-pattern")
    ResponseEntity<ContributionPatternResponse> getContributionPattern(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "연도별 분석 조회", description = "특정 연도의 GitHub 활동 분석을 조회합니다.")
    @GetMapping("/yearly-analysis")
    ResponseEntity<YearlyAnalysisResponse> getYearlyAnalysis(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId,
        @Parameter(description = "조회할 연도", required = true, example = "2024")
        @RequestParam int year
    );

    @Operation(summary = "저장소별 통계 조회", description = "사용자의 저장소별 기여 통계를 조회합니다.")
    @GetMapping("/repository-stats")
    ResponseEntity<RepositoryStatsResponse> getRepositoryStats(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId,
        @Parameter(description = "조회할 저장소 개수", example = "10")
        @RequestParam(defaultValue = "10") int limit,
        @Parameter(description = "정렬 기준 (commits, stars, lines)", example = "commits")
        @RequestParam(defaultValue = "commits") String sortBy
    );


    @Operation(summary = "언어 분포 조회 (Deprecated)", description = "이 기능은 더 이상 지원되지 않습니다.")
    @GetMapping("/language-distribution")
    ResponseEntity<Void> getLanguageDistribution(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "활동 타임라인 조회", description = "사용자의 최근 활동 타임라인을 조회합니다.")
    @GetMapping("/activity-timeline")
    ResponseEntity<ActivityTimelineResponse> getActivityTimeline(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId,
        @Parameter(description = "조회할 활동 개수", example = "20")
        @RequestParam(defaultValue = "20") int limit,
        @Parameter(description = "조회 기간 (일)", example = "90")
        @RequestParam(defaultValue = "90") int days
    );
*/
    @Operation(summary = "1. 최근 기여활동 조회", description = "사용자의 최근 기여 활동(최대 6개 저장소)을 조회합니다.")
    @GetMapping("/recent-activity")
    ResponseEntity<List<GithubRecentActivityResponse>> getRecentActivity(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "2. 전체 기여 내역 조회", description = "사용자의 전체 기여 내역을 조회합니다.")
    @GetMapping("/overall-history")
    ResponseEntity<GithubOverallHistoryResponse> getOverallHistory(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "3. 기여내역 비교 조회", description = "사용자와 전체 사용자 평균 기여 내역을 비교합니다.")
    @GetMapping("/contribution-comparison")
    ResponseEntity<GithubContributionComparisonResponse> getComparison(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "4. GitHub 기여점수 조회", description = "사용자의 GitHub 활동, 다양성, 영향력 점수를 조회합니다.")
    @GetMapping("/contribution-score")
    ResponseEntity<GithubContributionScoreResponse> getScore(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "전체 사용자 평균 통계 조회", description = "시스템 전체 사용자의 평균 기여 통계를 조회합니다.")
    @GetMapping("/global-statistics")
    ResponseEntity<GlobalStatisticsResponse> getGlobalStatistics(
        @Parameter(description = "사용자 ID (Context)", required = true)
        @PathVariable Long userId
    );
}
