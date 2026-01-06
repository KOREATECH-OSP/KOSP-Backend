package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document(collection = "github_pr_raw")
@CompoundIndex(name = "repo_pr_idx", def = "{'repoOwner': 1, 'repoName': 1, 'prNumber': 1}", unique = true)
@Getter
@Builder
public class GithubPRRaw {
    
    @Id
    private String id;
    
    private String repoOwner;
    private String repoName;
    private Integer prNumber;
    
    // Raw PR data from GitHub API
    private Map<String, Object> prData;
    
    @CreatedDate
    private LocalDateTime collectedAt;
    
    public static GithubPRRaw create(
        String repoOwner,
        String repoName,
        Integer prNumber,
        Map<String, Object> prData
    ) {
        return GithubPRRaw.builder()
            .repoOwner(repoOwner)
            .repoName(repoName)
            .prNumber(prNumber)
            .prData(prData)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
