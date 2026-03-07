package io.swkoreatech.kosp.client.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub GraphQL API의 사용자 이슈 목록 응답 DTO.
 *
 * <p>사용자가 생성한 이슈 목록과 각 이슈의 상세 정보(제목, 상태, 댓글 수, 저장소 정보)를 포함하며,
 * 커서 기반 페이지네이션을 지원한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserIssuesResponse {

    private User user;

    /**
     * 이슈 노드 목록을 반환한다.
     *
     * @return 이슈 노드 목록, 데이터 없으면 빈 목록
     */
    public List<IssueNode> getIssues() {
        if (user == null || user.getIssues() == null) {
            return Collections.emptyList();
        }
        return user.getIssues().getNodes();
    }

    /**
     * 페이지네이션 정보를 반환한다.
     *
     * @return 페이지 정보, 데이터 없으면 null
     */
    public PageInfo getPageInfo() {
        if (user == null || user.getIssues() == null) {
            return null;
        }
        return user.getIssues().getPageInfo();
    }

    /** 사용자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private Issues issues;
    }

    /** 이슈 목록과 페이지네이션 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issues {
        private PageInfo pageInfo;
        private List<IssueNode> nodes;
    }

    /** 페이지네이션 메타데이터를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private boolean hasNextPage;
        private String endCursor;
    }

    /** 개별 이슈 노드의 상세 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueNode {
        private Long number;
        private String title;
        private String state;
        private Instant createdAt;
        private Instant closedAt;
        private Comments comments;
        private Repository repository;

        /**
         * 댓글 수를 반환한다.
         *
         * @return 댓글 수, 정보 없으면 0
         */
        public int getCommentsCount() {
            return comments != null ? comments.getTotalCount() : 0;
        }

        /**
         * 저장소 이름을 반환한다.
         *
         * @return 저장소 이름, 정보 없으면 null
         */
        public String getRepoName() {
            return repository != null ? repository.getName() : null;
        }

        /**
         * 저장소 소유자 로그인 이름을 반환한다.
         *
         * @return 소유자 로그인 이름, 정보 없으면 null
         */
        public String getRepoOwner() {
            return repository != null && repository.getOwner() != null
                ? repository.getOwner().getLogin() : null;
        }

        /**
         * 저장소 전체 이름(owner/name)을 반환한다.
         *
         * @return 저장소 전체 이름, 정보 없으면 null
         */
        public String getRepoFullName() {
            return repository != null ? repository.getNameWithOwner() : null;
        }
    }

    /** 댓글 수 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comments {
        private int totalCount;
    }

    /** 저장소 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private String name;
        private Owner owner;
        private String nameWithOwner;
    }

    /** 저장소 소유자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private String login;
    }
}
