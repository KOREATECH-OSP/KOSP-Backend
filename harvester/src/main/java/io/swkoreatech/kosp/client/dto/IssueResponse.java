package io.swkoreatech.kosp.client.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * GitHub REST API의 이슈 응답 DTO.
 *
 * <p>이슈 번호, 제목, 상태, 댓글 수, 생성/종료 시각 정보를 포함하며,
 * 풀 리퀘스트와 연관된 이슈인지 여부를 판별할 수 있다.
 */
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

    /**
     * 이 이슈가 풀 리퀘스트와 연관되어 있는지 확인한다.
     *
     * @return 풀 리퀘스트이면 true
     */
    public boolean isPullRequest() {
        return pullRequest != null;
    }

    /** 풀 리퀘스트 참조 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequestRef {
        private String url;
    }
}
