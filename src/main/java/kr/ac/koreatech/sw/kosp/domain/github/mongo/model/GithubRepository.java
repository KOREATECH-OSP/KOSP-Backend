package kr.ac.koreatech.sw.kosp.domain.github.mongo.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Document(collection = "github_repositories")
public class GithubRepository {
    @Id
    private String id; // githubId + repoName (복합키 대신 String 조합 사용 권장)

    @Indexed
    private Long ownerGithubId; // 이 리포지토리 정보를 소유한 KOSP 유저의 GitHub ID

    private String name; // owner/repo_name
    private String description;
    private String language;
    private String url;

    private Integer stargazersCount;
    private Integer forksCount;

    private Integer myCommitCount; // 내가 기여한 커밋 수
    private Boolean isOwner; // 내가 소유자인가?

    private List<String> topics;
    
    private LocalDateTime lastCrawledAt;

    @Builder
    public GithubRepository(Long ownerGithubId, String name, String description, String language, String url, Integer stargazersCount, Integer forksCount, Integer myCommitCount, Boolean isOwner, List<String> topics) {
        this.ownerGithubId = ownerGithubId;
        this.name = name;
        this.id = ownerGithubId + "_" + name.replace("/", "_");
        this.description = description;
        this.language = language;
        this.url = url;
        this.stargazersCount = stargazersCount;
        this.forksCount = forksCount;
        this.myCommitCount = myCommitCount;
        this.isOwner = isOwner;
        this.topics = topics;
        this.lastCrawledAt = LocalDateTime.now();
    }
}
