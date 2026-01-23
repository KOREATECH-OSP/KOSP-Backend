package io.swkoreatech.kosp.domain.user.dto.response;

import java.util.List;

public record GithubActivityResponse(
    List<Activity> activities
) {
    public record Activity(
        String id, // Repo ID or Event ID
        String type, // "REPOSITORY", "PR", "ISSUE"
        String repoName,
        String title, // Repo description or Event payload
        String date, // LocalDateTime string
        String url
    ) {}
}
