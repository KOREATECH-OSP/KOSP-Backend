package kr.ac.koreatech.sw.kosp.infra.github.dto;

import java.util.List;

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

    public record ContributionCalendarNode(
        long totalContributions,
        List<WeekNode> weeks
    ) {}

    public record WeekNode(List<ContributionDayNode> contributionDays) {}
    public record ContributionDayNode(String date, int contributionCount) {}

    public record RepositoriesNode(
        long totalCount,
        PageInfo pageInfo,
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

    public record PrimaryLanguageNode(String name) {}
    
    public record LanguagesNode(List<LanguageEdge> edges) {}
    public record LanguageEdge(int size, PrimaryLanguageNode node) {}

    public record RepositoryStatsNode(int totalCount) {}
    
    public record DefaultBranchRefNode(TargetNode target) {}
    public record TargetNode(HistoryNode history) {}
    public record HistoryNode(
        int totalCount, 
        PageInfo pageInfo, 
        List<CommitEdge> edges
    ) {}
    public record CommitEdge(CommitNode node) {}
    public record CommitNode(
        String committedDate,
        AuthorNode author,
        int additions, 
        int deletions
    ) {}

    public record AuthorNode(UserSummaryNode user) {}
    public record UserSummaryNode(String login) {}

    public record PageInfo(boolean hasNextPage, String endCursor) {}
}
