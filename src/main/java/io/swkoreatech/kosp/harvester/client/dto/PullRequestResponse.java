package io.swkoreatech.kosp.harvester.client.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestResponse {

    private Long number;
    private String title;
    private String state;
    private int additions;
    private int deletions;

    @JsonProperty("changed_files")
    private int changedFiles;

    private int commits;
    private boolean merged;

    @JsonProperty("merged_at")
    private Instant mergedAt;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("closed_at")
    private Instant closedAt;
}
