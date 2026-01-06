package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document(collection = "github_issue_raw")
@CompoundIndex(name = "repo_issue_idx", def = "{'repoOwner': 1, 'repoName': 1, 'issueNumber': 1}", unique = true)
@Getter
@Builder
public class GithubIssueRaw {
    
    @Id
    private String id;
    
    private String repoOwner;
    private String repoName;
    private Integer issueNumber;
    
    // Raw issue data from GitHub API
    private Map<String, Object> issueData;
    
    @CreatedDate
    private LocalDateTime collectedAt;
    
    public static GithubIssueRaw create(
        String repoOwner,
        String repoName,
        Integer issueNumber,
        Map<String, Object> issueData
    ) {
        return GithubIssueRaw.builder()
            .repoOwner(repoOwner)
            .repoName(repoName)
            .issueNumber(issueNumber)
            .issueData(issueData)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
