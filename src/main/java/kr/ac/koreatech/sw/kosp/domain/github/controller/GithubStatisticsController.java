package kr.ac.koreatech.sw.kosp.domain.github.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.github.api.GithubStatisticsApi;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubMonthlyActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubRecentContributionsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubSummaryResponse;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GithubStatisticsController implements GithubStatisticsApi {

    private final GithubStatisticsService githubStatisticsService;

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
}
