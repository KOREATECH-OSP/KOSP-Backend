package kr.ac.koreatech.sw.kosp.domain.github.batch;

import kr.ac.koreatech.sw.kosp.domain.github.dto.UserSyncResult;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile.Stats;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.infra.github.client.GithubApiClient;
import kr.ac.koreatech.sw.kosp.infra.github.dto.GithubGraphQLResponse;
import kr.ac.koreatech.sw.kosp.infra.github.dto.GithubGraphQLResponse.RepositoryNode;
import kr.ac.koreatech.sw.kosp.infra.github.dto.GithubGraphQLResponse.UserNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubActivityProcessor implements ItemProcessor<User, UserSyncResult> {

    private final GithubApiClient githubApiClient;

    @Override
    public UserSyncResult process(User user) throws Exception {
        if (user.getGithubUser() == null || user.getGithubUser().getGithubToken() == null) {
            return null;
        }

        String token = user.getGithubUser().getGithubToken();
        String username = user.getGithubUser().getGithubLogin();
        Long githubId = user.getGithubUser().getGithubId();

        GithubGraphQLResponse response = githubApiClient.getFullUserActivity(token, username);
        if (response == null || response.data() == null || response.data().user() == null) {
            return null; // Token expired or API error
        }

        return mapToResult(githubId, response.data().user());
    }

    private UserSyncResult mapToResult(Long githubId, UserNode userNode) {
        List<GithubRepository> repositories = new ArrayList<>();
        Map<String, Long> userLanguageStats = new HashMap<>();
        long totalAdditions = 0;
        long totalDeletions = 0;
        long totalStars = 0;

        // Process Repositories
        if (userNode.repositories() != null && userNode.repositories().nodes() != null) {
            for (RepositoryNode repoNode : userNode.repositories().nodes()) {
                // Calculate Code Volume for this repo (from Default Branch History)
                long repoAdditions = 0;
                long repoDeletions = 0;
                int repoCommits = 0;

                if (repoNode.defaultBranchRef() != null && 
                    repoNode.defaultBranchRef().target() != null && 
                    repoNode.defaultBranchRef().target().history() != null) {
                    
                    var history = repoNode.defaultBranchRef().target().history();
                    repoCommits = history.totalCount(); // Total commits in default branch (approx)
                    
                    if (history.edges() != null) {
                        for (var edge : history.edges()) {
                            if (edge.node() != null) {
                                repoAdditions += edge.node().additions();
                                repoDeletions += edge.node().deletions();
                            }
                        }
                    }
                }

                // Sum up for total user stats
                totalAdditions += repoAdditions;
                totalDeletions += repoDeletions;
                totalStars += repoNode.stargazers().totalCount();

                // Process Languages
                Map<String, Long> repoLanguages = new HashMap<>();
                if (repoNode.languages() != null && repoNode.languages().edges() != null) {
                    for (var langEdge : repoNode.languages().edges()) {
                        String langName = langEdge.node().name();
                        long size = langEdge.size();
                        repoLanguages.put(langName, size);
                        userLanguageStats.merge(langName, size, Long::sum);
                    }
                }

                // Create GithubRepository Entity
                String primaryLang = repoNode.primaryLanguage() != null ? repoNode.primaryLanguage().name() : null;
                
                repositories.add(GithubRepository.builder()
                    .id(githubId + "/" + repoNode.name()) // manual compound ID
                    .ownerId(githubId)
                    .name(repoNode.name())
                    .url(repoNode.url())
                    .description(repoNode.description())
                    .homepageUrl(repoNode.homepageUrl())
                    .isFork(repoNode.isFork())
                    .primaryLanguage(primaryLang)
                    .languages(repoLanguages)
                    .stats(GithubRepository.RepositoryStats.builder()
                        .diskUsage(repoNode.diskUsage())
                        .stargazersCount(repoNode.stargazers().totalCount())
                        .forksCount(repoNode.forks().totalCount())
                        .watchersCount(repoNode.watchers().totalCount())
                        .openIssuesCount(repoNode.issues().totalCount())
                        .openPrsCount(repoNode.pullRequests().totalCount())
                        .build())
                    .codeVolume(GithubRepository.CodeVolume.builder()
                        .totalCommits(repoCommits)
                        .totalAdditions(repoAdditions)
                        .totalDeletions(repoDeletions)
                        .build())
                    .dates(GithubRepository.RepoDates.builder()
                        .createdAt(parseDateTime(repoNode.createdAt()))
                        .updatedAt(parseDateTime(repoNode.updatedAt()))
                        .pushedAt(parseDateTime(repoNode.pushedAt()))
                        .build())
                    .build());
            }
        }

        // Process User Profile
        long totalContributions = userNode.contributionsCollection().contributionCalendar().totalContributions();
        long totalCommitContributions = userNode.contributionsCollection().totalCommitContributions();
        long totalIssueContributions = userNode.contributionsCollection().totalIssueContributions();
        long totalPrContributions = userNode.contributionsCollection().totalPullRequestContributions();
        long totalRepoContributions = userNode.contributionsCollection().totalRepositoryContributions();

        int tier = calculateTier(totalStars, totalContributions);

        GithubProfile profile = GithubProfile.builder()
            .githubId(githubId)
            .bio(userNode.bio())
            .tier(tier)
            .followers((int) userNode.followers().totalCount())
            .following((int) userNode.following().totalCount())
            .score((double) totalContributions)
            .stats(Stats.builder()
                .totalCommits(totalCommitContributions) // Contribution count is different from repo commit sum
                .totalIssues(totalIssueContributions)
                .totalPrs(totalPrContributions)
                .totalStars(totalStars)
                .totalRepos(userNode.repositories().totalCount()) // Total including private/forks based on query
                .build())
            .totalAdditions(totalAdditions)
            .totalDeletions(totalDeletions)
            .languageStats(userLanguageStats)
            .build();

        return new UserSyncResult(profile, repositories);
    }

    private int calculateTier(long stars, long contributions) {
        long score = stars * 10 + contributions;
        if (score > 1000) return 3; // Gold
        if (score > 300) return 2;  // Silver
        return 1;                   // Bronze
    }

    private LocalDateTime parseDateTime(String isoString) {
        if (isoString == null) return null;
        try {
            return LocalDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}
