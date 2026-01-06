package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document(collection = "github_commit_raw")
@CompoundIndex(name = "repo_commit_idx", def = "{'repoOwner': 1, 'repoName': 1, 'sha': 1}", unique = true)
@Getter
@Builder
public class GithubCommitRaw {
    
    @Id
    private String id;
    
    private String repoOwner;
    private String repoName;
    private String sha;
    
    // Raw commit data from GitHub API
    private Map<String, Object> commitData;
    
    @CreatedDate
    private LocalDateTime collectedAt;
    
    public static GithubCommitRaw create(
        String repoOwner,
        String repoName,
        String sha,
        Map<String, Object> commitData
    ) {
        return GithubCommitRaw.builder()
            .repoOwner(repoOwner)
            .repoName(repoName)
            .sha(sha)
            .commitData(commitData)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
