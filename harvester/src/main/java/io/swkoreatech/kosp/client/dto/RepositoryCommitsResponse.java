package io.swkoreatech.kosp.client.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryCommitsResponse {

    private Repository repository;

    public List<CommitNode> getCommits() {
        if (repository == null || repository.getDefaultBranchRef() == null) {
            return Collections.emptyList();
        }
        var target = repository.getDefaultBranchRef().getTarget();
        if (target == null || target.getHistory() == null) {
            return Collections.emptyList();
        }
        return target.getHistory().getNodes();
    }

    public PageInfo getPageInfo() {
        if (repository == null || repository.getDefaultBranchRef() == null) {
            return null;
        }
        var target = repository.getDefaultBranchRef().getTarget();
        if (target == null || target.getHistory() == null) {
            return null;
        }
        return target.getHistory().getPageInfo();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private DefaultBranchRef defaultBranchRef;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DefaultBranchRef {
        private Target target;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Target {
        private History history;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class History {
        private PageInfo pageInfo;
        private List<CommitNode> nodes;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private boolean hasNextPage;
        private String endCursor;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitNode {
        private String oid;
        private String message;
        private int additions;
        private int deletions;
        private Integer changedFilesIfAvailable;
        private Instant authoredDate;
        private Author author;

        public String getAuthorName() {
            return author != null ? author.getName() : null;
        }

        public String getAuthorEmail() {
            return author != null ? author.getEmail() : null;
        }

        public int getChangedFiles() {
            return changedFilesIfAvailable != null ? changedFilesIfAvailable : 0;
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
    }
}
