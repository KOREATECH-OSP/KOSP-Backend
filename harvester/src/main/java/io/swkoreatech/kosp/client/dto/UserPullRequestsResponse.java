package io.swkoreatech.kosp.client.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub GraphQL API의 사용자 풀 리퀘스트 목록 응답 DTO.
 *
 * <p>사용자가 생성한 PR 목록과 각 PR의 상세 정보(코드 변경 통계, 병합 상태,
 * 커밋 수, 저장소 정보)를 포함하며, 커서 기반 페이지네이션을 지원한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPullRequestsResponse {

    private User user;

    /**
     * PR 노드 목록을 반환한다.
     *
     * @return PR 노드 목록, 데이터 없으면 빈 목록
     */
    public List<PullRequestNode> getPullRequests() {
        if (user == null || user.getPullRequests() == null) {
            return Collections.emptyList();
        }
        return user.getPullRequests().getNodes();
    }

    /**
     * 페이지네이션 정보를 반환한다.
     *
     * @return 페이지 정보, 데이터 없으면 null
     */
    public PageInfo getPageInfo() {
        if (user == null || user.getPullRequests() == null) {
            return null;
        }
        return user.getPullRequests().getPageInfo();
    }

    /** 사용자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private PullRequests pullRequests;
    }

    /** PR 목록과 페이지네이션 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequests {
        private PageInfo pageInfo;
        private List<PullRequestNode> nodes;
    }

    /** 페이지네이션 메타데이터를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private boolean hasNextPage;
        private String endCursor;
    }

    /** 개별 PR 노드의 상세 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequestNode {
        private Long number;
        private String title;
        private String state;
        private int additions;
        private int deletions;
        private int changedFiles;
        private boolean merged;
        private boolean isCrossRepository;
        private Instant mergedAt;
        private Instant createdAt;
        private Instant closedAt;
        private Commits commits;
        private ClosingIssuesReferences closingIssuesReferences;
        private Repository repository;

        /**
         * 커밋 수를 반환한다.
         *
         * @return 커밋 수, 정보 없으면 0
         */
        public int getCommitsCount() {
            return commits != null ? commits.getTotalCount() : 0;
        }

        /**
         * 닫힌 이슈 수를 반환한다.
         *
         * @return 닫힌 이슈 수, 정보 없으면 0
         */
        public int getClosedIssuesCount() {
            return closingIssuesReferences != null ? closingIssuesReferences.getTotalCount() : 0;
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

        /**
         * 저장소 스타 수를 반환한다.
         *
         * @return 스타 수, 정보 없으면 0
         */
        public int getRepoStarCount() {
            return repository != null ? repository.getStargazerCount() : 0;
        }
    }

    /** 커밋 수 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commits {
        private int totalCount;
    }

    /** 닫힌 이슈 참조 수를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClosingIssuesReferences {
        private int totalCount;
    }

    /** 저장소 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private String name;
        private Owner owner;
        private String nameWithOwner;
        private int stargazerCount;
    }

    /** 저장소 소유자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private String login;
    }
}
