package kr.ac.koreatech.sw.kosp.domain.github.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.github.api.GithubApi;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubContributionComparisonResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubContributionScoreResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubOverallHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubRecentActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GlobalStatisticsResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GithubController implements GithubApi {

    // TODO: GithubStatisticsService 재구축 후 연결

    @Override
    public ResponseEntity<List<GithubRecentActivityResponse>> getRecentActivity(Long userId) {
        // TODO: Implement after statistics service is rebuilt
        return ResponseEntity.ok(List.of());
    }

    @Override
    public ResponseEntity<GithubOverallHistoryResponse> getOverallHistory(Long userId) {
        // TODO: Implement after statistics service is rebuilt
        return ResponseEntity.ok(new GithubOverallHistoryResponse(0, 0, 0, 0, 0, 0));
    }

    @Override
    public ResponseEntity<GithubContributionComparisonResponse> getComparison(Long userId) {
        // TODO: Implement after statistics service is rebuilt
        return ResponseEntity.ok(new GithubContributionComparisonResponse(0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0));
    }

    @Override
    public ResponseEntity<GithubContributionScoreResponse> getScore(Long userId) {
        // TODO: Implement after statistics service is rebuilt
        return ResponseEntity.ok(new GithubContributionScoreResponse(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Override
    public ResponseEntity<GlobalStatisticsResponse> getGlobalStatistics(Long userId) {
        // TODO: Implement after statistics service is rebuilt
        return ResponseEntity.ok(GlobalStatisticsResponse.builder()
            .avgCommitCount(0.0)
            .avgStarCount(0.0)
            .avgPrCount(0.0)
            .avgIssueCount(0.0)
            .totalUsers(0)
            .calculatedAt(LocalDateTime.now())
            .build());
    }
}
