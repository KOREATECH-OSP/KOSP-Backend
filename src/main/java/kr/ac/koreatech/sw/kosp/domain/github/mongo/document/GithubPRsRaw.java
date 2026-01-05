package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document(collection = "github_prs_raw")
@CompoundIndex(name = "repo_idx", def = "{'repoOwner': 1, 'repoName': 1}")
@Getter
@Builder
public class GithubPRsRaw {

    @Id
    private String id;

    private String repoOwner;
    private String repoName;

    // PR 목록 (배열로 저장)
    private List<Map<String, Object>> pullRequests;

    // 메타데이터
    private LocalDateTime collectedAt;

    public static GithubPRsRaw create(
        String repoOwner,
        String repoName,
        List<Map<String, Object>> pullRequests
    ) {
        return GithubPRsRaw.builder()
            .repoOwner(repoOwner)
            .repoName(repoName)
            .pullRequests(pullRequests)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
