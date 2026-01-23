package io.swkoreatech.kosp.harvester.client.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueResponse {

    private Long number;
    private String title;
    private String state;
    private int comments;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("closed_at")
    private Instant closedAt;

    @JsonProperty("pull_request")
    private PullRequestRef pullRequest;

    public boolean isPullRequest() {
        return pullRequest != null;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequestRef {
        private String url;
    }
}
