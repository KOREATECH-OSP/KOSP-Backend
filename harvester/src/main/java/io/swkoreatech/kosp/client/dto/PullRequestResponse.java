package io.swkoreatech.kosp.client.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * GitHub REST API의 풀 리퀘스트 상세 응답 DTO.
 *
 * <p>PR 번호, 제목, 상태, 코드 변경 통계(추가/삭제/변경 파일 수),
 * 병합 여부 및 시각 정보를 포함한다.
 */
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
