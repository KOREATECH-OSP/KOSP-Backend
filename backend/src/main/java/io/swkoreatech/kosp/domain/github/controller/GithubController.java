package io.swkoreatech.kosp.domain.github.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.github.api.GithubApi;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionComparisonResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubContributionScoreResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubOverallHistoryResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubRecentActivityResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GlobalStatisticsResponse;
import io.swkoreatech.kosp.domain.github.service.GithubStatisticsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GithubController implements GithubApi {

    private final GithubStatisticsService statisticsService;

    @Override
    public ResponseEntity<List<GithubRecentActivityResponse>> getRecentActivity(Long userId) {
        return ResponseEntity.ok(List.of());
    }

    @Override
    public ResponseEntity<GithubOverallHistoryResponse> getOverallHistory(Long userId) {
        return ResponseEntity.ok(statisticsService.getOverallHistory(userId));
    }

    @Override
    public ResponseEntity<GithubContributionComparisonResponse> getComparison(Long userId) {
        return ResponseEntity.ok(statisticsService.getComparison(userId));
    }

    @Override
    public ResponseEntity<GithubContributionScoreResponse> getScore(Long userId) {
        return ResponseEntity.ok(statisticsService.getScore(userId));
    }

    @Override
    public ResponseEntity<GlobalStatisticsResponse> getGlobalStatistics(Long userId) {
        return ResponseEntity.ok(statisticsService.getGlobalStatistics());
    }
}
