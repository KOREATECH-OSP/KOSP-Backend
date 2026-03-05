package io.swkoreatech.kosp.domain.github.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.github.model.PlatformStatistics;
import lombok.Builder;
import lombok.Getter;

/**
 * 전체 사용자 통계 응답 DTO.
 * 시스템 전체 사용자의 평균 기여 통계를 제공한다.
 */
@Getter
@Builder
public class GlobalStatisticsResponse {

    private Double avgCommitCount;
    private Double avgStarCount;
    private Double avgPrCount;
    private Double avgIssueCount;
    private Integer totalUsers;
    private LocalDateTime calculatedAt;

    /**
     * 플랫폼 통계로부터 전체 사용자 통계 응답을 생성한다.
     *
     * @param stats 플랫폼 통계
     * @return 전체 사용자 통계 응답
     */
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
