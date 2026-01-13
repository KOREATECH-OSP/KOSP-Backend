package kr.ac.koreatech.sw.kosp.domain.github.model;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "github_language_statistics",
    indexes = {
        @Index(name = "idx_github_id", columnList = "githubId"),
        @Index(name = "idx_language", columnList = "language"),
        @Index(name = "idx_github_id_language", columnList = "githubId,language", unique = true)
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubLanguageStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String githubId;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(nullable = false)
    private Integer linesOfCode = 0;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer repositories = 0;

    @Column(nullable = false)
    private Integer commits = 0;

    // 메타
    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    public static GithubLanguageStatistics create(String githubId, String language) {
        GithubLanguageStatistics statistics = new GithubLanguageStatistics();
        statistics.githubId = githubId;
        statistics.language = language;
        statistics.calculatedAt = LocalDateTime.now();
        return statistics;
    }

    public void updateStatistics(
        Integer linesOfCode,
        BigDecimal percentage,
        Integer repositories,
        Integer commits
    ) {
        this.linesOfCode = linesOfCode;
        this.percentage = percentage;
        this.repositories = repositories;
        this.commits = commits;
        this.calculatedAt = LocalDateTime.now();
    }
}
