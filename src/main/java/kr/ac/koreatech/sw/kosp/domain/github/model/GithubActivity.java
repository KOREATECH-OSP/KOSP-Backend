package kr.ac.koreatech.sw.kosp.domain.github.model;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Embedded;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "github_activity")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Embedded
    private GithubStats stats;

    @Builder
    private GithubActivity(User user, GithubStats stats) {
        this.user = user;
        this.stats = stats != null ? stats : new GithubStats();
    }

    public void update(Long totalCommits, Long totalPrs, Long totalIssues, Long totalStars, Long repoCount) {
        this.stats = GithubStats.builder()
            .totalCommits(totalCommits != null ? totalCommits : this.stats.getTotalCommits())
            .totalPrs(totalPrs != null ? totalPrs : this.stats.getTotalPrs())
            .totalIssues(totalIssues != null ? totalIssues : this.stats.getTotalIssues())
            .totalStars(totalStars != null ? totalStars : this.stats.getTotalStars())
            .repoCount(repoCount != null ? repoCount : this.stats.getRepoCount())
            .build();
    }
}
