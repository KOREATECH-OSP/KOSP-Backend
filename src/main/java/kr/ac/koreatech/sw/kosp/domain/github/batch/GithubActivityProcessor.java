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

        return mapToResult(githubId, response.data().user(), username);
    }

    private UserSyncResult mapToResult(Long githubId, UserNode userNode, String username) {
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

        int tier = calculateTier(totalStars, totalContributions);

        GithubProfile profile = GithubProfile.builder()
            .githubId(githubId)
            .bio(userNode.bio())
            .tier(tier)
            .followers((int) userNode.followers().totalCount())
            .following((int) userNode.following().totalCount())
            .score((double) totalContributions)
            .stats(Stats.builder()
                .totalCommits(totalCommitContributions)
                .totalIssues(totalIssueContributions)
                .totalPrs(totalPrContributions)
                .totalStars(totalStars)
                .totalRepos(userNode.repositories().totalCount())
                .build())
            .totalAdditions(totalAdditions)
            .totalDeletions(totalDeletions)
            .languageStats(userLanguageStats)
            .analysis(calculateAnalysis(userNode, username))
            .build();

        return new UserSyncResult(profile, repositories);
    }

    private int calculateTier(long stars, long contributions) {
        long score = stars * 10 + contributions;
        if (score > 1000) return 3; // Gold
        if (score > 300) return 2;  // Silver
        return 1;                   // Bronze
    }

    private GithubProfile.Analysis calculateAnalysis(UserNode userNode, String myUsername) {
        Map<String, Integer> monthlyContributions = calculateMonthlyContributions(userNode);
        
        CommitAnalysisResult commitAnalysis = analyzeCommitHistory(userNode, myUsername);
        Map<Integer, Integer> timeOfDayStats = commitAnalysis.timeOfDayStats;
        Map<String, Integer> dayOfWeekStats = commitAnalysis.dayOfWeekStats;
        Map<String, Integer> collaborators = commitAnalysis.collaborators;

        String workingStyle = determineWorkingStyle(timeOfDayStats);
        String collaborationStyle = collaborators.isEmpty() ? "Independent" : "Team Player";

        GithubProfile.BestRepoSummary bestRepo = findBestRepository(userNode);

        return GithubProfile.Analysis.builder()
            .monthlyContributions(monthlyContributions)
            .timeOfDayStats(timeOfDayStats)
            .dayOfWeekStats(dayOfWeekStats)
            .collaborators(collaborators)
            .workingStyle(workingStyle)
            .collaborationStyle(collaborationStyle)
            .bestRepository(bestRepo)
            .build();
    }

    private Map<String, Integer> calculateMonthlyContributions(UserNode userNode) {
        Map<String, Integer> monthlyContributions = new HashMap<>();
        if (userNode.contributionsCollection() == null || 
            userNode.contributionsCollection().contributionCalendar() == null ||
            userNode.contributionsCollection().contributionCalendar().weeks() == null) {
            return monthlyContributions;
        }

        for (var week : userNode.contributionsCollection().contributionCalendar().weeks()) {
            if (week.contributionDays() != null) {
                for (var day : week.contributionDays()) {
                    if (day.contributionCount() > 0) {
                        String month = day.date().substring(0, 7); // YYYY-MM
                        monthlyContributions.merge(month, day.contributionCount(), Integer::sum);
                    }
                }
            }
        }
        return monthlyContributions;
    }

    private record CommitAnalysisResult(
        Map<Integer, Integer> timeOfDayStats,
        Map<String, Integer> dayOfWeekStats,
        Map<String, Integer> collaborators
    ) {}

    private CommitAnalysisResult analyzeCommitHistory(UserNode userNode, String myUsername) {
        Map<Integer, Integer> timeOfDayStats = new HashMap<>();
        Map<String, Integer> dayOfWeekStats = new HashMap<>();
        Map<String, Integer> collaborators = new HashMap<>();

        if (userNode.repositories() != null && userNode.repositories().nodes() != null) {
            for (var repo : userNode.repositories().nodes()) {
                processRepositoryCommits(repo, myUsername, timeOfDayStats, dayOfWeekStats, collaborators);
            }
        }
        return new CommitAnalysisResult(timeOfDayStats, dayOfWeekStats, collaborators);
    }

    private void processRepositoryCommits(RepositoryNode repo, String myUsername, 
                                          Map<Integer, Integer> timeStats, 
                                          Map<String, Integer> dayStats, 
                                          Map<String, Integer> collaborators) {
        if (repo.defaultBranchRef() == null || repo.defaultBranchRef().target() == null || 
            repo.defaultBranchRef().target().history() == null || repo.defaultBranchRef().target().history().edges() == null) {
            return;
        }

        for (var edge : repo.defaultBranchRef().target().history().edges()) {
            if (edge.node() == null || edge.node().committedDate() == null) continue;

            LocalDateTime committedAt = parseDateTime(edge.node().committedDate());
            if (committedAt == null) continue;

            String author = (edge.node().author() != null && edge.node().author().user() != null) 
                            ? edge.node().author().user().login() : "unknown";

            if (myUsername.equals(author)) {
                timeStats.merge(committedAt.getHour(), 1, Integer::sum);
                dayStats.merge(committedAt.getDayOfWeek().name(), 1, Integer::sum);
            } else if (!"unknown".equals(author)) {
                collaborators.merge(author, 1, Integer::sum);
            }
        }
    }

    private GithubProfile.BestRepoSummary findBestRepository(UserNode userNode) {
        if (userNode.repositories() == null || userNode.repositories().nodes() == null) return null;

        RepositoryNode bestNode = null;
        long maxScore = -1;

        for (var repo : userNode.repositories().nodes()) {
            long score = calculateRepoScore(repo);
            if (score > maxScore) {
                maxScore = score;
                bestNode = repo;
            }
        }

        if (bestNode == null) return null;

        return buildBestRepoSummary(bestNode);
    }

    private long calculateRepoScore(RepositoryNode repo) {
        long commits = 0;
        long lines = 0;
        if (repo.defaultBranchRef() != null && repo.defaultBranchRef().target() != null && repo.defaultBranchRef().target().history() != null) {
            commits = repo.defaultBranchRef().target().history().totalCount();
            if (repo.defaultBranchRef().target().history().edges() != null) {
                for (var edge : repo.defaultBranchRef().target().history().edges()) {
                     if (edge.node() != null) {
                         lines += (edge.node().additions() + edge.node().deletions());
                     }
                }
            }
        }
        return commits * 10 + lines;
    }

    private GithubProfile.BestRepoSummary buildBestRepoSummary(RepositoryNode repo) {
        long commits = 0;
        long lines = 0;
        // Re-calculate to populate fields (or reuse if performance is key, but here cleanliness is preferred)
        // ... (Logic duplicated for clean method structure, or pass result)
        // Optimization: Let's just recalculate since it's only done once for the best repo.
        
        if (repo.defaultBranchRef() != null && repo.defaultBranchRef().target() != null && repo.defaultBranchRef().target().history() != null) {
            commits = repo.defaultBranchRef().target().history().totalCount();
            if (repo.defaultBranchRef().target().history().edges() != null) {
                for (var edge : repo.defaultBranchRef().target().history().edges()) {
                        if (edge.node() != null) {
                            lines += (edge.node().additions() + edge.node().deletions());
                        }
                }
            }
        }

        return GithubProfile.BestRepoSummary.builder()
            .name(repo.name())
            .totalCommits(commits)
            .totalLines(lines)
            .totalPrs((long) repo.pullRequests().totalCount())
            .totalIssues((long) repo.issues().totalCount())
            .build();
    }

    private String determineWorkingStyle(Map<Integer, Integer> timeStats) {
        long nightCommits = 0;
        long dayCommits = 0;
        for (var entry : timeStats.entrySet()) {
            int hour = entry.getKey();
            int count = entry.getValue();
            if (hour >= 22 || hour < 6) nightCommits += count;
            else dayCommits += count;
        }
        return nightCommits > dayCommits ? "Night Owl" : "Day Walker";
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
