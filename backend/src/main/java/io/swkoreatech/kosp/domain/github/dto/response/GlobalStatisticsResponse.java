package io.swkoreatech.kosp.domain.github.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.github.model.PlatformStatistics;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GlobalStatisticsResponse {
    
    private Double avgCommitCount;
    private Double avgStarCount;
    private Double avgPrCount;
    private Double avgIssueCount;
    private Integer totalUsers;
    private LocalDateTime calculatedAt;

    public static GlobalStatisticsResponse from(PlatformStatistics stats) {
        if (stats == null) {
            return GlobalStatisticsResponse.builder()
                .avgCommitCount(0.0)
                .avgStarCount(0.0)
                .avgPrCount(0.0)
                .avgIssueCount(0.0)
                .totalUsers(0)
                .calculatedAt(LocalDateTime.now())
                .build();
        }
        return GlobalStatisticsResponse.builder()
            .avgCommitCount(stats.getAvgCommitCount().doubleValue())
            .avgStarCount(stats.getAvgStarCount().doubleValue())
            .avgPrCount(stats.getAvgPrCount().doubleValue())
            .avgIssueCount(stats.getAvgIssueCount().doubleValue())
            .totalUsers(stats.getTotalUserCount())
            .calculatedAt(stats.getCalculatedAt())
            .build();
    }
}
