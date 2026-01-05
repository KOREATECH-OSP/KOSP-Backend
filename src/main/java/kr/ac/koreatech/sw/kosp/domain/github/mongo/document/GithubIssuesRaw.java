package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document(collection = "github_issues_raw")
@CompoundIndex(name = "repo_idx", def = "{'repoOwner': 1, 'repoName': 1}")
@Getter
@Builder
public class GithubIssuesRaw {

    @Id
    private String id;

    private String repoOwner;
    private String repoName;

    // 이슈 목록 (배열로 저장)
    private List<Map<String, Object>> issues;

    // 메타데이터
    private LocalDateTime collectedAt;

    public static GithubIssuesRaw create(
        String repoOwner,
        String repoName,
        List<Map<String, Object>> issues
    ) {
        return GithubIssuesRaw.builder()
            .repoOwner(repoOwner)
            .repoName(repoName)
            .issues(issues)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
