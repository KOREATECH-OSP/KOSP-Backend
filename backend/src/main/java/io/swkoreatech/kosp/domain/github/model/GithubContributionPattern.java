package io.swkoreatech.kosp.domain.github.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "github_contribution_pattern",
    indexes = {
        @Index(name = "idx_github_id", columnList = "githubId", unique = true)
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubContributionPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String githubId;

    // 시간대 패턴
    @Column(nullable = false)
    private Integer nightOwlScore = 0;

    @Column(nullable = false)
    private Integer nightCommits = 0;

    @Column(nullable = false)
    private Integer dayCommits = 0;

    // 프로젝트 패턴
    @Column(nullable = false)
    private Integer initiatorScore = 0;

    @Column(nullable = false)
    private Integer earlyContributions = 0;

    @Column(nullable = false)
    private Integer independentScore = 0;

    @Column(nullable = false)
    private Integer soloProjects = 0;

    @Column(nullable = false)
    private Integer totalProjects = 0;

    // 협업 패턴
    @Column(nullable = false)
    private Integer totalCoworkers = 0;

    // 시간대별 분포 (JSON)
    @Column(columnDefinition = "TEXT")
    private String hourlyDistribution;

    // 메타
    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    public static GithubContributionPattern create(String githubId) {
        GithubContributionPattern pattern = new GithubContributionPattern();
        pattern.githubId = githubId;
        pattern.calculatedAt = LocalDateTime.now();
        return pattern;
    }

    public void updateTimePattern(
        Integer nightOwlScore,
        Integer nightCommits,
        Integer dayCommits,
        String hourlyDistribution
    ) {
        this.nightOwlScore = nightOwlScore;
        this.nightCommits = nightCommits;
        this.dayCommits = dayCommits;
        this.hourlyDistribution = hourlyDistribution;
        this.calculatedAt = LocalDateTime.now();
    }

    public void updateProjectPattern(
        Integer initiatorScore,
        Integer earlyContributions,
        Integer independentScore,
        Integer soloProjects,
        Integer totalProjects
    ) {
        this.initiatorScore = initiatorScore;
        this.earlyContributions = earlyContributions;
        this.independentScore = independentScore;
        this.soloProjects = soloProjects;
        this.totalProjects = totalProjects;
        this.calculatedAt = LocalDateTime.now();
    }

    public void updateCollaborationPattern(Integer totalCoworkers) {
        this.totalCoworkers = totalCoworkers;
        this.calculatedAt = LocalDateTime.now();
    }
}
