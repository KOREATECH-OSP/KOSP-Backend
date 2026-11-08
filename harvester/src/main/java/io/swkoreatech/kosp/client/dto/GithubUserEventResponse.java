package io.swkoreatech.kosp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub REST API의 사용자 이벤트 응답 DTO.
 *
 * <p>PushEvent에서 레포지토리 이름(owner/repo)을 추출하여
 * default 브랜치 외 feature 브랜치 커밋 레포를 발견하는 데 사용된다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUserEventResponse {

    private String type;
    private Repo repo;

    /** 이벤트 대상 저장소 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repo {
        private String name; // "owner/repo-name" 형식
    }
}
