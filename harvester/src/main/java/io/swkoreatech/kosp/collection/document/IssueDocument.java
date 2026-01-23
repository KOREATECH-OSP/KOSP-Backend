package io.swkoreatech.kosp.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document(collection = "github_issues")
@CompoundIndex(name = "user_repo_idx", def = "{'userId': 1, 'repositoryName': 1}")
public class IssueDocument {

    @Id
    private String id;

    private Long userId;
    private Long issueNumber;
    private String title;
    private String state;
    private String repositoryName;
    private String repositoryOwner;

    private Integer commentsCount;

    private Instant createdAt;
    private Instant closedAt;

    private Instant collectedAt;
}
