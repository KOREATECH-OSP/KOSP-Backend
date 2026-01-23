package io.swkoreatech.kosp.client.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPullRequestsResponse {

    private User user;

    public List<PullRequestNode> getPullRequests() {
        if (user == null || user.getPullRequests() == null) {
            return Collections.emptyList();
        }
        return user.getPullRequests().getNodes();
    }

    public PageInfo getPageInfo() {
        if (user == null || user.getPullRequests() == null) {
            return null;
        }
        return user.getPullRequests().getPageInfo();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private PullRequests pullRequests;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequests {
        private PageInfo pageInfo;
        private List<PullRequestNode> nodes;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private boolean hasNextPage;
        private String endCursor;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequestNode {
        private Long number;
        private String title;
        private String state;
        private int additions;
        private int deletions;
        private int changedFiles;
        private boolean merged;
        private boolean isCrossRepository;
        private Instant mergedAt;
        private Instant createdAt;
        private Instant closedAt;
        private Commits commits;
        private ClosingIssuesReferences closingIssuesReferences;
        private Repository repository;

        public int getCommitsCount() {
            return commits != null ? commits.getTotalCount() : 0;
        }

        public int getClosedIssuesCount() {
            return closingIssuesReferences != null ? closingIssuesReferences.getTotalCount() : 0;
        }

        public String getRepoName() {
            return repository != null ? repository.getName() : null;
        }

        public String getRepoOwner() {
            return repository != null && repository.getOwner() != null 
                ? repository.getOwner().getLogin() : null;
        }

        public String getRepoFullName() {
            return repository != null ? repository.getNameWithOwner() : null;
        }

        public int getRepoStarCount() {
            return repository != null ? repository.getStargazerCount() : 0;
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commits {
        private int totalCount;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClosingIssuesReferences {
        private int totalCount;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private String name;
        private Owner owner;
        private String nameWithOwner;
        private int stargazerCount;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private String login;
    }
}
