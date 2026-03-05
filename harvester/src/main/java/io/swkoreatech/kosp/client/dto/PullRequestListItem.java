package io.swkoreatech.kosp.client.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * GitHub REST API의 풀 리퀘스트 목록 항목 응답 DTO.
 *
 * <p>PR 번호, 제목, 상태, 생성 시각을 포함한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestListItem {

    private Long number;
    private String title;
    private String state;

    @JsonProperty("created_at")
    private Instant createdAt;
}
