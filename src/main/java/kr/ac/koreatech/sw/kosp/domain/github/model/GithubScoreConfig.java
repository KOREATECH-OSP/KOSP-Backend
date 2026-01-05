package kr.ac.koreatech.sw.kosp.domain.github.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_score_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubScoreConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_name", unique = true, nullable = false, length = 100)
    private String configName;

    @Column(nullable = false)
    private Boolean active = false;

    // 활동 수준 (최대 3점)
    @Column(name = "activity_level_max_score", nullable = false)
    private Double activityLevelMaxScore = 3.0;

    @Column(name = "commits_weight", nullable = false)
    private Double commitsWeight = 0.01;

    @Column(name = "lines_weight", nullable = false)
    private Double linesWeight = 0.0001;

    // 활동 다양성 (최대 1점)
    @Column(name = "diversity_max_score", nullable = false)
    private Double diversityMaxScore = 1.0;

    @Column(name = "diversity_repo_threshold", nullable = false)
    private Integer diversityRepoThreshold = 10;

    // 활동 영향성 (최대 5점)
    @Column(name = "impact_max_score", nullable = false)
    private Double impactMaxScore = 5.0;

    @Column(name = "stars_weight", nullable = false)
    private Double starsWeight = 0.01;

    @Column(name = "forks_weight", nullable = false)
    private Double forksWeight = 0.05;

    @Column(name = "contributors_weight", nullable = false)
    private Double contributorsWeight = 0.02;

    // 보너스 점수
    @Column(name = "night_owl_bonus", nullable = false)
    private Double nightOwlBonus = 0.5;

    @Column(name = "early_adopter_bonus", nullable = false)
    private Double earlyAdopterBonus = 0.3;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Builder
    public GithubScoreConfig(
        String configName,
        Boolean active,
        Double activityLevelMaxScore,
        Double commitsWeight,
        Double linesWeight,
        Double diversityMaxScore,
        Integer diversityRepoThreshold,
        Double impactMaxScore,
        Double starsWeight,
        Double forksWeight,
        Double contributorsWeight,
        Double nightOwlBonus,
        Double earlyAdopterBonus,
        String createdBy
    ) {
        this.configName = configName;
        this.active = active != null ? active : false;
        this.activityLevelMaxScore = activityLevelMaxScore != null ? activityLevelMaxScore : 3.0;
        this.commitsWeight = commitsWeight != null ? commitsWeight : 0.01;
        this.linesWeight = linesWeight != null ? linesWeight : 0.0001;
        this.diversityMaxScore = diversityMaxScore != null ? diversityMaxScore : 1.0;
        this.diversityRepoThreshold = diversityRepoThreshold != null ? diversityRepoThreshold : 10;
        this.impactMaxScore = impactMaxScore != null ? impactMaxScore : 5.0;
        this.starsWeight = starsWeight != null ? starsWeight : 0.01;
        this.forksWeight = forksWeight != null ? forksWeight : 0.05;
        this.contributorsWeight = contributorsWeight != null ? contributorsWeight : 0.02;
        this.nightOwlBonus = nightOwlBonus != null ? nightOwlBonus : 0.5;
        this.earlyAdopterBonus = earlyAdopterBonus != null ? earlyAdopterBonus : 0.3;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(
        Double activityLevelMaxScore,
        Double commitsWeight,
        Double linesWeight,
        Double diversityMaxScore,
        Integer diversityRepoThreshold,
        Double impactMaxScore,
        Double starsWeight,
        Double forksWeight,
        Double contributorsWeight,
        Double nightOwlBonus,
        Double earlyAdopterBonus
    ) {
        if (activityLevelMaxScore != null) this.activityLevelMaxScore = activityLevelMaxScore;
        if (commitsWeight != null) this.commitsWeight = commitsWeight;
        if (linesWeight != null) this.linesWeight = linesWeight;
        if (diversityMaxScore != null) this.diversityMaxScore = diversityMaxScore;
        if (diversityRepoThreshold != null) this.diversityRepoThreshold = diversityRepoThreshold;
        if (impactMaxScore != null) this.impactMaxScore = impactMaxScore;
        if (starsWeight != null) this.starsWeight = starsWeight;
        if (forksWeight != null) this.forksWeight = forksWeight;
        if (contributorsWeight != null) this.contributorsWeight = contributorsWeight;
        if (nightOwlBonus != null) this.nightOwlBonus = nightOwlBonus;
        if (earlyAdopterBonus != null) this.earlyAdopterBonus = earlyAdopterBonus;
        this.updatedAt = LocalDateTime.now();
    }
}
