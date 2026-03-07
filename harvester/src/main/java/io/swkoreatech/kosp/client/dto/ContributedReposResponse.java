package io.swkoreatech.kosp.client.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub GraphQL API의 기여 저장소 조회 응답 DTO.
 *
 * <p>사용자가 커밋, PR, 이슈를 통해 기여한 저장소 목록과
 * 각 저장소의 상세 정보(스타 수, 포크 수, 주 언어 등)를 포함한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContributedReposResponse {

    private User user;

    /**
     * 모든 기여 유형(커밋, PR, 이슈)의 저장소를 수집하여 반환한다.
     *
     * @return 기여 저장소 정보 집합
     */
    public Set<RepositoryInfo> collectAllRepositories() {
        if (user == null) {
            return Collections.emptySet();
        }
        return user.collectAllRepositories();
    }

    /**
     * 사용자의 GitHub 노드 ID를 반환한다.
     *
     * @return 사용자 노드 ID, 정보 없으면 null
     */
    public String getUserNodeId() {
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    /** GitHub 사용자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String id;
        private ContributionsCollection contributionsCollection;

        /**
         * 사용자의 모든 기여 저장소를 수집한다.
         *
         * @return 기여 저장소 정보 집합
         */
        public Set<RepositoryInfo> collectAllRepositories() {
            if (contributionsCollection == null) {
                return Collections.emptySet();
            }
            return contributionsCollection.collectAllRepositories();
        }
    }

    /** 사용자 기여 컬렉션(커밋, PR, 이슈)을 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContributionsCollection {
        private List<RepoContribution> commitContributionsByRepository;
        private List<RepoContribution> pullRequestContributionsByRepository;
        private List<RepoContribution> issueContributionsByRepository;

        /**
         * 모든 기여 유형의 저장소를 하나의 집합으로 수집한다.
         *
         * @return 중복 제거된 저장소 정보 집합
         */
        public Set<RepositoryInfo> collectAllRepositories() {
            Set<RepositoryInfo> allRepos = new HashSet<>();
            allRepos.addAll(collectFromList(commitContributionsByRepository));
            allRepos.addAll(collectFromList(pullRequestContributionsByRepository));
            allRepos.addAll(collectFromList(issueContributionsByRepository));
            return allRepos;
        }

        private Set<RepositoryInfo> collectFromList(List<RepoContribution> contributions) {
            if (contributions == null) {
                return Collections.emptySet();
            }
            return contributions.stream()
                .map(RepoContribution::getRepository)
                .collect(Collectors.toSet());
        }

        /**
         * 지정된 저장소의 커밋 기여 수를 반환한다.
         *
         * @param repoFullName "owner/name" 형식의 저장소 전체 이름
         * @return 커밋 기여 수
         */
        public int getCommitCount(String repoFullName) {
            return getContributionCount(commitContributionsByRepository, repoFullName);
        }

        /**
         * 지정된 저장소의 PR 기여 수를 반환한다.
         *
         * @param repoFullName "owner/name" 형식의 저장소 전체 이름
         * @return PR 기여 수
         */
        public int getPrCount(String repoFullName) {
            return getContributionCount(pullRequestContributionsByRepository, repoFullName);
        }

        /**
         * 지정된 저장소의 이슈 기여 수를 반환한다.
         *
         * @param repoFullName "owner/name" 형식의 저장소 전체 이름
         * @return 이슈 기여 수
         */
        public int getIssueCount(String repoFullName) {
            return getContributionCount(issueContributionsByRepository, repoFullName);
        }

        private int getContributionCount(List<RepoContribution> list, String repoFullName) {
            if (list == null) {
                return 0;
            }
            return list.stream()
                .filter(c -> c.getRepository().getNameWithOwner().equals(repoFullName))
                .findFirst()
                .map(c -> c.getContributions().getTotalCount())
                .orElse(0);
        }
    }

    /** 저장소별 기여 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepoContribution {
        private RepositoryInfo repository;
        private Contributions contributions;
    }

    /** 기여 수를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contributions {
        private int totalCount;
    }

    /** 저장소 상세 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoryInfo {
        private String name;
        private String description;
        private Owner owner;
        private String nameWithOwner;
        private boolean isFork;
        private boolean isPrivate;
        private PrimaryLanguage primaryLanguage;
        private int stargazerCount;
        private int forkCount;
        private String createdAt;
        private WatchersInfo watchers;

        /**
         * 저장소 소유자의 로그인 이름을 반환한다.
         *
         * @return 소유자 로그인 이름, 정보 없으면 null
         */
        public String getOwnerLogin() {
            return owner != null ? owner.getLogin() : null;
        }

        /**
         * 주 사용 언어 이름을 반환한다.
         *
         * @return 언어 이름, 정보 없으면 null
         */
        public String getLanguageName() {
            return primaryLanguage != null ? primaryLanguage.getName() : null;
        }

        /**
         * 워처 수를 반환한다.
         *
         * @return 워처 수, 정보 없으면 0
         */
        public Integer getWatchersCount() {
            return watchers != null ? watchers.getTotalCount() : 0;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RepositoryInfo that)) {
                return false;
            }
            return nameWithOwner != null && nameWithOwner.equals(that.nameWithOwner);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return nameWithOwner != null ? nameWithOwner.hashCode() : 0;
        }
    }

    /** 워처 수 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WatchersInfo {
        private int totalCount;
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
}
