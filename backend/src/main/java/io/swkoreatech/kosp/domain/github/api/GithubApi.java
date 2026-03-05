package io.swkoreatech.kosp.domain.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionComparisonResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionScoreResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubOverallHistoryResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubRecentActivityResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GlobalStatisticsResponse;

/**
 * GitHub 통계 API.
 * 사용자의 GitHub 기여 활동, 통계, 점수 조회 기능을 정의한다.
 */
@Tag(name = "GitHub", description = "GitHub 관련 API")
@RequestMapping("/v1/users/{userId}/github")
public interface GithubApi {

    /**
     * 사용자의 최근 기여 활동을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 최근 기여 활동 목록
     */
    @Operation(
        summary = "최근 기여활동 조회",
        description = "사용자의 최근 기여 활동(최대 6개 저장소)을 조회합니다."
    )
    @GetMapping("/recent-activity")
    ResponseEntity<List<GithubRecentActivityResponse>> getRecentActivity(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    /**
     * 사용자의 전체 기여 내역을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 전체 기여 내역 응답
     */
    @Operation(summary = "전체 기여 내역 조회", description = "사용자의 전체 기여 내역을 조회합니다.")
    @GetMapping("/overall-history")
    ResponseEntity<GithubOverallHistoryResponse> getOverallHistory(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    /**
     * 사용자와 전체 평균의 기여 내역을 비교 조회한다.
     *
     * @param userId 사용자 ID
     * @return 기여 내역 비교 응답
     */
    @Operation(
        summary = "기여내역 비교 조회",
        description = "사용자와 전체 사용자 평균 기여 내역을 비교합니다."
    )
    @GetMapping("/contribution-comparison")
    ResponseEntity<GithubContributionComparisonResponse> getComparison(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    /**
     * 사용자의 GitHub 기여 점수를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 기여 점수 응답
     */
    @Operation(
        summary = "GitHub 기여점수 조회",
        description = "사용자의 GitHub 활동, 다양성, 영향력 점수를 조회합니다."
    )
    @GetMapping("/contribution-score")
    ResponseEntity<GithubContributionScoreResponse> getScore(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    /**
     * 전체 사용자의 평균 기여 통계를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 전체 통계 응답
     */
    @Operation(
        summary = "전체 사용자 평균 통계 조회",
        description = "시스템 전체 사용자의 평균 기여 통계를 조회합니다."
    )
    @GetMapping("/global-statistics")
    ResponseEntity<GlobalStatisticsResponse> getGlobalStatistics(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );
}
