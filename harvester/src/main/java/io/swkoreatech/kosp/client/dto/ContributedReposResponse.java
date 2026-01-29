package io.swkoreatech.kosp.client.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContributedReposResponse {

    private User user;

    public Set<RepositoryInfo> collectAllRepositories() {
        if (user == null) {
            return Collections.emptySet();
        }
        return user.collectAllRepositories();
    }

    public String getUserNodeId() {
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String id;
        private ContributionsCollection contributionsCollection;

        public Set<RepositoryInfo> collectAllRepositories() {
            if (contributionsCollection == null) {
                return Collections.emptySet();
            }
            return contributionsCollection.collectAllRepositories();
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContributionsCollection {
        private List<RepoContribution> commitContributionsByRepository;
        private List<RepoContribution> pullRequestContributionsByRepository;
        private List<RepoContribution> issueContributionsByRepository;

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

        public int getCommitCount(String repoFullName) {
            return getContributionCount(commitContributionsByRepository, repoFullName);
        }

        public int getPrCount(String repoFullName) {
            return getContributionCount(pullRequestContributionsByRepository, repoFullName);
        }

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

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepoContribution {
        private RepositoryInfo repository;
        private Contributions contributions;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contributions {
        private int totalCount;
    }

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

         public String getOwnerLogin() {
             return owner != null ? owner.getLogin() : null;
         }

         public String getLanguageName() {
             return primaryLanguage != null ? primaryLanguage.getName() : null;
         }

         public Integer getWatchersCount() {
             return watchers != null ? watchers.getTotalCount() : 0;
         }

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

        @Override
        public int hashCode() {
            return nameWithOwner != null ? nameWithOwner.hashCode() : 0;
        }
    }

     @Getter
     @JsonIgnoreProperties(ignoreUnknown = true)
     public static class WatchersInfo {
         private int totalCount;
     }

     @Getter
     @JsonIgnoreProperties(ignoreUnknown = true)
     public static class Owner {
         private String login;
     }

     @Getter
     @JsonIgnoreProperties(ignoreUnknown = true)
     public static class PrimaryLanguage {
         private String name;
     }
}
