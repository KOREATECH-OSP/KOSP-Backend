package kr.ac.koreatech.sw.kosp.domain.github.mongo.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Getter
@Document(collection = "github_trends")
public class GithubTrend {
    @Id
    private String id; // githubId + period (e.g. "12345_2024-12")

    @Field("github_id")
    private Long githubId;

    private String period; // YYYY-MM
    
    private LocalDate startDate; // 검색 편의용

    private Integer commits;
    private Integer pullRequests;
    private Integer issues;
    private Integer starsEarned;
    private Integer contributedRepoCount;

    @Builder
    public GithubTrend(Long githubId, String period, LocalDate startDate, Integer commits, Integer pullRequests, Integer issues, Integer starsEarned, Integer contributedRepoCount) {
        this.githubId = githubId;
        this.period = period;
        this.id = githubId + "_" + period;
        this.startDate = startDate;
        this.commits = commits;
        this.pullRequests = pullRequests;
        this.issues = issues;
        this.starsEarned = starsEarned;
        this.contributedRepoCount = contributedRepoCount;
    }
}
