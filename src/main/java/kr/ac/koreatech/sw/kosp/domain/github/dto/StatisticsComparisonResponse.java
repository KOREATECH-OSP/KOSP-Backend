package kr.ac.koreatech.sw.kosp.domain.github.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatisticsComparisonResponse {

    // 평균 통계
    private Double avgCommits;
    private Double avgStars;
    private Double avgPrs;
    private Double avgIssues;
    private Double avgLines;
    private Double avgScore;

    // 나의 통계
    private Integer myCommits;
    private Integer myStars;
    private Integer myPrs;
    private Integer myIssues;
    private Integer myLines;
    private Double myScore;

    // 순위
    private Integer commitsRank;
    private Integer starsRank;
    private Integer prsRank;
    private Integer issuesRank;
    private Integer linesRank;
    private Integer totalScoreRank;

    // 백분위 (상위 몇 %)
    private Double commitsPercentile;
    private Double starsPercentile;
    private Double prsPercentile;
    private Double issuesPercentile;
    private Double linesPercentile;
    private Double totalScorePercentile;

    // 전체 사용자 수
    private Integer totalUsers;
}
