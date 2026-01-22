package io.swkoreatech.kosp.harvester.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document(collection = "github_contributed_repos")
public class ContributedRepoDocument {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String repositoryName;
    private String repositoryOwner;
    private String fullName;
    private String description;

    private Boolean isOwner;
    private Boolean isFork;
    private Boolean isPrivate;

    private String primaryLanguage;
    private Integer stargazersCount;
    private Integer forksCount;

    private Integer userCommitCount;
    private Integer userPrCount;
    private Integer userIssueCount;

    private Instant lastContributedAt;
    private Instant collectedAt;
}
