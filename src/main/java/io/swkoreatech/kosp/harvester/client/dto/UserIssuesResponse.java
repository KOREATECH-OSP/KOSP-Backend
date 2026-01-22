package io.swkoreatech.kosp.harvester.client.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserIssuesResponse {

    private User user;

    public List<IssueNode> getIssues() {
        if (user == null || user.getIssues() == null) {
            return Collections.emptyList();
        }
        return user.getIssues().getNodes();
    }

    public PageInfo getPageInfo() {
        if (user == null || user.getIssues() == null) {
            return null;
        }
        return user.getIssues().getPageInfo();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private Issues issues;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issues {
        private PageInfo pageInfo;
        private List<IssueNode> nodes;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private boolean hasNextPage;
        private String endCursor;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueNode {
        private Long number;
        private String title;
        private String state;
        private Instant createdAt;
        private Instant closedAt;
        private Comments comments;
        private Repository repository;

        public int getCommentsCount() {
            return comments != null ? comments.getTotalCount() : 0;
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
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comments {
        private int totalCount;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private String name;
        private Owner owner;
        private String nameWithOwner;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private String login;
    }
}
