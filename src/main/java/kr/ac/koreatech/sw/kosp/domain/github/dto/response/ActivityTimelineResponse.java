package kr.ac.koreatech.sw.kosp.domain.github.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelineData;
import lombok.Builder;

public record ActivityTimelineResponse(
    List<TimelineItem> timeline,
    Integer totalItems
) {
    @Builder
    public record TimelineItem(
        String type,              // "PR", "ISSUE"
        LocalDate date,
        String repository,
        String title,
        Integer number
    ) {}

    public static ActivityTimelineResponse from(List<GithubTimelineData> timelineData) {
        List<TimelineItem> items = timelineData.stream()
            .flatMap(data -> {
                List<TimelineItem> dataItems = new java.util.ArrayList<>();
                
                // PRs
                if (data.getPrs() != null) {
                    data.getPrs().forEach(pr -> {
                        TimelineItem item = TimelineItem.builder()
                            .type("PR")
                            .date(pr.getDate())
                            .repository(pr.getOwnerId() + "/" + pr.getRepoName())
                            .title(pr.getTitle())
                            .number(pr.getNumber())
                            .build();
                        dataItems.add(item);
                    });
                }
                
                // Issues
                if (data.getIssues() != null) {
                    data.getIssues().forEach(issue -> {
                        TimelineItem item = TimelineItem.builder()
                            .type("ISSUE")
                            .date(issue.getDate())
                            .repository(issue.getOwnerId() + "/" + issue.getRepoName())
                            .title(issue.getTitle())
                            .number(issue.getNumber())
                            .build();
                        dataItems.add(item);
                    });
                }
                
                return dataItems.stream();
            })
            .sorted((a, b) -> b.date().compareTo(a.date()))
            .toList();

        return new ActivityTimelineResponse(items, items.size());
    }
}
