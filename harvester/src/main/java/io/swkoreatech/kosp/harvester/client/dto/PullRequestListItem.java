package io.swkoreatech.kosp.harvester.client.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestListItem {

    private Long number;
    private String title;
    private String state;

    @JsonProperty("created_at")
    private Instant createdAt;
}
