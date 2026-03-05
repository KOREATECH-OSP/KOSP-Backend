package io.swkoreatech.kosp.domain.github.controller;

import io.swkoreatech.kosp.domain.github.api.GithubApi;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionComparisonResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionScoreResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubOverallHistoryResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubRecentActivityResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GlobalStatisticsResponse;
import io.swkoreatech.kosp.domain.github.service.GithubStatisticsService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * GitHub 통계 컨트롤러.
 * {@link GithubApi}의 구현체로, GitHub 기여 활동 및 통계 조회 요청을 처리한다.
 */
@RestController
@RequiredArgsConstructor
public class GithubController implements GithubApi {

    private final GithubStatisticsService statisticsService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<GithubRecentActivityResponse>> getRecentActivity(Long userId) {
        return ResponseEntity.ok(statisticsService.getRecentActivity(userId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<GithubOverallHistoryResponse> getOverallHistory(Long userId) {
        return ResponseEntity.ok(statisticsService.getOverallHistory(userId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<GithubContributionComparisonResponse> getComparison(Long userId) {
        return ResponseEntity.ok(statisticsService.getComparison(userId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<GithubContributionScoreResponse> getScore(Long userId) {
        return ResponseEntity.ok(statisticsService.getScore(userId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<GlobalStatisticsResponse> getGlobalStatistics(Long userId) {
        return ResponseEntity.ok(statisticsService.getGlobalStatistics());
    }
}
