package kr.ac.koreatech.sw.kosp.domain.github.mongo.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Document(collection = "github_profiles")
public class GithubProfile {
    @Id
    private Long githubId; // MySQL GithubUser의 githubId와 동일

    private String bio;
    
    private Integer tier;

    private Integer followers;
    private Integer following;

    private List<String> achievements; // e.g. ["Pull Shark", "Quickdraw"]

    private Stats stats;

    private Double score; // 랭킹용 기여도 점수

    // Expanded Stats (Overall)
    private Long totalAdditions;
    private Long totalDeletions;
    private Map<String, Long> languageStats; // Overall Language Usage (Bytes)

    private Map<String, Object> extraData; // 확장용 비정형 데이터

    private LocalDateTime updatedAt;

    @Builder
    public GithubProfile(Long githubId, String bio, Integer tier, Integer followers, Integer following, List<String> achievements, Stats stats, Double score, Map<String, Object> extraData, Long totalAdditions, Long totalDeletions, Map<String, Long> languageStats) {
        this.githubId = githubId;
        this.bio = bio;
        this.tier = tier;
        this.followers = followers;
        this.following = following;
        this.achievements = achievements;
        this.stats = stats;
        this.score = score;
        this.extraData = extraData;
        this.totalAdditions = totalAdditions;
        this.totalDeletions = totalDeletions;
        this.languageStats = languageStats;
        this.updatedAt = LocalDateTime.now();
    }

    @Getter
    @Builder
    public static class Stats {
        private Long totalCommits;
        private Long totalIssues;
        private Long totalPrs;
        private Long totalStars; // 받은 스타 수
        private Long totalRepos;
    }
}
