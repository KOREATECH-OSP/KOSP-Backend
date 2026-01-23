package io.swkoreatech.kosp.client.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitResponse {

    private String sha;
    private CommitInfo commit;
    private Stats stats;

    public String getMessage() {
        if (commit == null) {
            return null;
        }
        return commit.getMessage();
    }

    public String getAuthorName() {
        if (commit == null || commit.getAuthor() == null) {
            return null;
        }
        return commit.getAuthor().getName();
    }

    public String getAuthorEmail() {
        if (commit == null || commit.getAuthor() == null) {
            return null;
        }
        return commit.getAuthor().getEmail();
    }

    public Instant getAuthoredAt() {
        if (commit == null || commit.getAuthor() == null) {
            return null;
        }
        return commit.getAuthor().getDate();
    }

    public int getAdditions() {
        if (stats == null) {
            return 0;
        }
        return stats.getAdditions();
    }

    public int getDeletions() {
        if (stats == null) {
            return 0;
        }
        return stats.getDeletions();
    }

    public int getChangedFiles() {
        if (stats == null) {
            return 0;
        }
        return stats.getTotal();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitInfo {
        private String message;
        private Author author;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
        private Instant date;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
        private int additions;
        private int deletions;
        private int total;
    }
}
