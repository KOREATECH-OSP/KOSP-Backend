package kr.ac.koreatech.sw.kosp.domain.github.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubMonthlyActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubRecentContributionsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubSummaryResponse;

@Tag(name = "GitHub Statistics", description = "GitHub 통계 API")
@RequestMapping("/v1/users/{userId}/github")
public interface GithubStatisticsApi {

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
}
