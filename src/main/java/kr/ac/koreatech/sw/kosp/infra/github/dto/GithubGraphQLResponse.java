package kr.ac.koreatech.sw.kosp.infra.github.dto;

import java.util.List;
import java.util.Map;

// Nested Records for GraphQL Response Mapping
public record GithubGraphQLResponse(
    DataNode data
) {
    public record DataNode(UserNode user) {}

    public record UserNode(
        String bio,
        String company,
        FollowersNode followers,
        FollowingNode following,
        ContributionsCollectionNode contributionsCollection,
        RepositoriesNode repositories
    ) {}

    public record FollowersNode(long totalCount) {}
    public record FollowingNode(long totalCount) {}

    public record ContributionsCollectionNode(
        ContributionCalendarNode contributionCalendar,
        long totalCommitContributions,
        long totalIssueContributions,
        long totalPullRequestContributions,
        long totalRepositoryContributions
    ) {}

    public record ContributionCalendarNode(long totalContributions) {}

    public record RepositoriesNode(
        long totalCount,
        List<RepositoryNode> nodes
    ) {}

    public record RepositoryNode(
        String name,
        String url,
        String description,
        String homepageUrl,
        boolean isFork,
        PrimaryLanguageNode primaryLanguage,
        LanguagesNode languages,
        RepositoryStatsNode stargazers,
        RepositoryStatsNode forks,
        RepositoryStatsNode watchers,
        RepositoryStatsNode issues,
        RepositoryStatsNode pullRequests,
        int diskUsage,
        String createdAt,
        String updatedAt,
        String pushedAt,
        DefaultBranchRefNode defaultBranchRef
    ) {}
    
    // Helper needed because diskUsage is direct field, not object in some contexts, 
    // but here we map from Map<String, Object> usually. 
    // Wait, RestClient with records maps JSON directly.
    // In GraphQL: 
    // repositories { nodes { diskUsage } } -> simply int.
    // So RepositoryNode should have 'int diskUsage'.
    // See revised RepositoryNode below.
    
    public record PrimaryLanguageNode(String name) {}
    
    public record LanguagesNode(List<LanguageEdge> edges) {}
    public record LanguageEdge(int size, PrimaryLanguageNode node) {}

    public record RepositoryStatsNode(int totalCount) {}
    
    // Handling diskUsage (int) directly in RepositoryNode
    // public record DiskUsageNode... No.

    public record DefaultBranchRefNode(TargetNode target) {}
    public record TargetNode(HistoryNode history) {}
    public record HistoryNode(int totalCount, List<CommitEdge> edges) {}
    public record CommitEdge(CommitNode node) {}
    public record CommitNode(int additions, int deletions) {}

}
