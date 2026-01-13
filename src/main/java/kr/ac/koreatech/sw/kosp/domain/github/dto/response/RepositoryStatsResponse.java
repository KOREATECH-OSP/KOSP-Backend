package kr.ac.koreatech.sw.kosp.domain.github.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import lombok.Builder;

public record RepositoryStatsResponse(
    List<RepositoryInfo> repositories,
    SummaryInfo summary
) {
    @Builder
    public record RepositoryInfo(
        String owner,
        String name,
        String fullName,
        Boolean isMainRepository,
        String repositoryType,
        Integer userCommits,
        Integer userPrs,
        Integer userIssues,
        Integer stargazersCount,
        Integer forksCount,
        String primaryLanguage,
        LocalDateTime lastCommitDate,
        Double contributionPercentage
    ) {}

    @Builder
    public record SummaryInfo(
        Integer totalRepositories,
        Integer ownedRepositories,
        Integer contributedRepositories
    ) {}

    public static RepositoryStatsResponse from(
        List<GithubRepositoryStatistics> repositories,
        List<String> mainRepoKeys
    ) {
        List<RepositoryInfo> repoInfos = repositories.stream()
            .map(repo -> {
                String fullName = repo.getRepoOwner() + "/" + repo.getRepoName();
                boolean isMain = mainRepoKeys.contains(fullName);
                
                // 기여 비율 계산
                double contributionPercentage = 0.0;
                if (repo.getTotalCommitsCount() != null && repo.getTotalCommitsCount() > 0) {
                    contributionPercentage = (double) repo.getUserCommitsCount() / repo.getTotalCommitsCount() * 100;
                }

                // 저장소 타입 결정
                String repoType = repo.getRepoOwner().equalsIgnoreCase(repo.getContributorGithubId()) 
                    ? "OWNED" 
                    : "CONTRIBUTED";

                return RepositoryInfo.builder()
                    .owner(repo.getRepoOwner())
                    .name(repo.getRepoName())
                    .fullName(fullName)
                    .isMainRepository(isMain)
                    .repositoryType(repoType)
                    .userCommits(repo.getUserCommitsCount())
                    .userPrs(repo.getUserPrsCount())
                    .userIssues(repo.getUserIssuesCount())
                    .stargazersCount(repo.getStargazersCount())
                    .forksCount(repo.getForksCount())
                    .primaryLanguage(repo.getPrimaryLanguage())
                    .lastCommitDate(repo.getLastCommitDate())
                    .contributionPercentage(Math.round(contributionPercentage * 10.0) / 10.0)
                    .build();
            })
            .toList();

        long ownedCount = repoInfos.stream()
            .filter(r -> "OWNED".equals(r.repositoryType()))
            .count();

        long contributedCount = repoInfos.stream()
            .filter(r -> "CONTRIBUTED".equals(r.repositoryType()))
            .count();

        SummaryInfo summary = SummaryInfo.builder()
            .totalRepositories(repoInfos.size())
            .ownedRepositories((int) ownedCount)
            .contributedRepositories((int) contributedCount)
            .build();

        return new RepositoryStatsResponse(repoInfos, summary);
    }
}
