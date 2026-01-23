package io.swkoreatech.kosp.harvester.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitListItem {

    private String sha;
    private CommitInfo commit;

    public String getMessage() {
        if (commit == null) {
            return null;
        }
        return commit.getMessage();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitInfo {
        private String message;
    }
}
