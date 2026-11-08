package io.swkoreatech.kosp.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * GitHub Search Commits API 응답 DTO.
 *
 * <p>GET /search/commits?q=author:{login} 응답을 매핑한다.
 * feature 브랜치에만 커밋한 레포지토리도 author 기준으로 탐지할 수 있어
 * contributionsCollection API가 잡지 못하는 케이스를 보완한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommitSearchResponse {

    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("incomplete_results")
    private boolean incompleteResults;

    private List<CommitItem> items;

    /** 검색 결과의 개별 커밋 항목. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitItem {
        private String sha;
        private Repository repository;
    }

    /** 커밋이 속한 저장소 정보. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("private")
        private boolean isPrivate;
    }
}
