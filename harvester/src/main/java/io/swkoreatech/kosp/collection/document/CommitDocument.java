package io.swkoreatech.kosp.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document(collection = "github_commits")
@CompoundIndex(name = "user_repo_idx", def = "{'userId': 1, 'repositoryName': 1}")
@CompoundIndex(name = "unique_commit_idx", def = "{'userId': 1, 'repositoryName': 1, 'sha': 1}", unique = true)
public class CommitDocument {

    @Id
    private String id;

    private Long userId;
    private String sha;
    private String message;
    private String repositoryName;
    private String repositoryOwner;

    private String authorName;
    private String authorEmail;
    private Instant authoredAt;

    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;

    private Instant collectedAt;
}
