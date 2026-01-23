package io.swkoreatech.kosp.harvester.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document(collection = "github_pull_requests")
@CompoundIndex(name = "user_repo_idx", def = "{'userId': 1, 'repositoryName': 1}")
public class PullRequestDocument {

    @Id
    private String id;

    private Long userId;
    private Long prNumber;
    private String title;
    private String state;
    private String repositoryName;
    private String repositoryOwner;

    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private Integer commitsCount;
    private Integer repoStarCount;
    private Integer closedIssuesCount;

    private Boolean merged;
    private Boolean isCrossRepository;
    private Instant mergedAt;
    private Instant createdAt;
    private Instant closedAt;

    private Instant collectedAt;
}
