package kr.ac.koreatech.sw.kosp.domain.github.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "github_monthly_statistics",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_month",
        columnNames = {"github_id", "year", "month"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubMonthlyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String githubId;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer commitsCount = 0;

    @Column(nullable = false)
    private Integer linesCount = 0;

    @Column(nullable = false)
    private Integer additionsCount = 0;

    @Column(nullable = false)
    private Integer deletionsCount = 0;

    @Column(nullable = false)
    private Integer prsCount = 0;

    @Column(nullable = false)
    private Integer issuesCount = 0;

    @Column(nullable = false)
    private Integer createdReposCount = 0;

    @Column(nullable = false)
    private Integer contributedReposCount = 0;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    public static GithubMonthlyStatistics create(String githubId, Integer year, Integer month) {
        GithubMonthlyStatistics statistics = new GithubMonthlyStatistics();
        statistics.githubId = githubId;
        statistics.year = year;
        statistics.month = month;
        statistics.calculatedAt = LocalDateTime.now();
        return statistics;
    }

    public void updateStatistics(
        Integer commitsCount,
        Integer linesCount,
        Integer additionsCount,
        Integer deletionsCount,
        Integer prsCount,
        Integer issuesCount,
        Integer createdReposCount,
        Integer contributedReposCount
    ) {
        this.commitsCount = commitsCount;
        this.linesCount = linesCount;
        this.additionsCount = additionsCount;
        this.deletionsCount = deletionsCount;
        this.prsCount = prsCount;
        this.issuesCount = issuesCount;
        this.createdReposCount = createdReposCount;
        this.contributedReposCount = contributedReposCount;
        this.calculatedAt = LocalDateTime.now();
    }
}
