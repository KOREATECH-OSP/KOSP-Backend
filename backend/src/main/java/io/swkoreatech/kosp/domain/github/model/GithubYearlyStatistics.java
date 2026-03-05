package io.swkoreatech.kosp.domain.github.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GitHub 연도별 통계 엔티티.
 * 사용자의 연도별 기여 통계, 점수, 순위 정보를 관리한다.
 */
@Entity
@Table(
    name = "github_yearly_statistics",
    indexes = {
        @Index(name = "idx_github_id", columnList = "githubId"),
        @Index(name = "idx_year", columnList = "\"year\""),
        @Index(name = "idx_github_id_year", columnList = "githubId,\"year\"", unique = true)
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubYearlyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String githubId;

    @Column(name = "\"year\"", nullable = false)
    private Integer year;

    // 연도별 통계
    @Column(nullable = false)
    private Integer commits;

    @Column(nullable = false)
    private Integer lines;

    @Column(nullable = false)
    private Integer additions;

    @Column(nullable = false)
    private Integer deletions;

    @Column(nullable = false)
    private Integer prs;

    @Column(nullable = false)
    private Integer issues;

    // 연도별 점수
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalScore;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mainRepoScore;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal otherRepoScore;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prIssueScore;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal reputationScore;

    // 순위
    private Integer rank;

    private Integer percentile;

    // 최고 저장소
    private String bestRepoOwner;

    private String bestRepoName;

    private Integer bestRepoCommits;

    // 메타
    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @Builder
    private GithubYearlyStatistics(String githubId, Integer year) {
        this.githubId = githubId;
        this.year = year;
        this.commits = 0;
        this.lines = 0;
        this.additions = 0;
        this.deletions = 0;
        this.prs = 0;
        this.issues = 0;
        this.totalScore = BigDecimal.ZERO;
        this.mainRepoScore = BigDecimal.ZERO;
        this.otherRepoScore = BigDecimal.ZERO;
        this.prIssueScore = BigDecimal.ZERO;
        this.reputationScore = BigDecimal.ZERO;
        this.calculatedAt = LocalDateTime.now();
    }

    /**
     * 연도별 통계 수치를 갱신한다.
     *
     * @param commits 커밋 수
     * @param lines 코드 라인 수
     * @param additions 추가 라인 수
     * @param deletions 삭제 라인 수
     * @param prs PR 수
     * @param issues 이슈 수
     */
    public void updateStatistics(
        Integer commits,
        Integer lines,
        Integer additions,
        Integer deletions,
        Integer prs,
        Integer issues
    ) {
        this.commits = commits;
        this.lines = lines;
        this.additions = additions;
        this.deletions = deletions;
        this.prs = prs;
        this.issues = issues;
        this.calculatedAt = LocalDateTime.now();
    }

    /**
     * 연도별 점수를 갱신한다.
     *
     * @param mainRepoScore 메인 저장소 점수
     * @param otherRepoScore 기타 저장소 점수
     * @param prIssueScore PR/이슈 점수
     * @param reputationScore 평판 점수
     */
    public void updateScores(
        BigDecimal mainRepoScore,
        BigDecimal otherRepoScore,
        BigDecimal prIssueScore,
        BigDecimal reputationScore
    ) {
        this.mainRepoScore = mainRepoScore;
        this.otherRepoScore = otherRepoScore;
        this.prIssueScore = prIssueScore;
        this.reputationScore = reputationScore;
        this.totalScore = mainRepoScore
            .add(otherRepoScore)
            .add(prIssueScore)
            .add(reputationScore);
    }

    /**
     * 순위 정보를 갱신한다.
     *
     * @param rank 순위
     * @param percentile 백분위
     */
    public void updateRanking(Integer rank, Integer percentile) {
        this.rank = rank;
        this.percentile = percentile;
    }

    /**
     * 최고 저장소 정보를 갱신한다.
     *
     * @param owner 저장소 소유자
     * @param name 저장소 이름
     * @param commits 커밋 수
     */
    public void updateBestRepository(String owner, String name, Integer commits) {
        this.bestRepoOwner = owner;
        this.bestRepoName = name;
        this.bestRepoCommits = commits;
    }
}
