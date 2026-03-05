package io.swkoreatech.kosp.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

/**
 * GitHub GraphQL API의 사용자 기본 정보 응답 DTO.
 *
 * <p>사용자 로그인, 이름, 계정 생성/수정 시각, 소유 저장소 목록을 포함하며,
 * 저장소 목록은 커서 기반 페이지네이션을 지원한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBasicInfoResponse {

    private User user;
    
    /** 사용자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String login;
        private String name;
        private String createdAt;
        private String updatedAt;
        private RepositoriesData repositories;
    }
    
    /** 저장소 목록과 페이지네이션 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoriesData {
        private int totalCount;
        private PageInfo pageInfo;
        private List<RepositoryNode> nodes;
    }
    
    /** 페이지네이션 메타데이터를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private boolean hasNextPage;
        private String endCursor;
    }
    
    /** 개별 저장소 노드 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoryNode {
        private String name;
        private String nameWithOwner;
        private String description;
        private Owner owner;
        private boolean isFork;
        private boolean isPrivate;
        private PrimaryLanguage primaryLanguage;
        private int stargazerCount;
        private int forkCount;
        private Watchers watchers;
        private String createdAt;
        private String updatedAt;
    }
    
    /** 저장소 소유자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private String login;
    }
    
    /** 주 사용 프로그래밍 언어 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrimaryLanguage {
        private String name;
    }
    
    /** 워처 수 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Watchers {
        private int totalCount;
    }
}
