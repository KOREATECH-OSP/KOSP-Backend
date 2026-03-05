package io.swkoreatech.kosp.domain.github.dto.response;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

/**
 * 저장소 통계 응답 DTO.
 *
 * @param repositories 저장소 정보 목록
 * @param summary 요약 정보
 */
public record RepositoryStatsResponse(
    List<RepositoryInfo> repositories,
    SummaryInfo summary
) {
    /**
     * 저장소 상세 정보.
     *
     * @param owner 소유자
     * @param name 저장소 이름
     * @param fullName 전체 이름 (소유자/이름)
     * @param isMainRepository 메인 저장소 여부
     * @param repositoryType 저장소 유형 (OWNED/CONTRIBUTED)
     * @param userCommits 사용자 커밋 수
     * @param userPrs 사용자 PR 수
     * @param userIssues 사용자 이슈 수
     * @param stargazersCount 스타 수
     * @param forksCount 포크 수
     * @param primaryLanguage 주요 프로그래밍 언어
     * @param lastCommitDate 마지막 커밋 일시
     * @param contributionPercentage 기여 비율
     */
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

    /**
     * 저장소 요약 정보.
     *
     * @param totalRepositories 전체 저장소 수
     * @param ownedRepositories 소유 저장소 수
     * @param contributedRepositories 기여 저장소 수
     */
    @Builder
    public record SummaryInfo(
        Integer totalRepositories,
        Integer ownedRepositories,
        Integer contributedRepositories
    ) {}

    /**
     * 저장소 통계 목록과 메인 저장소 키 목록으로부터 응답을 생성한다.
     *
     * @param repositories 저장소 통계 목록
     * @param mainRepoKeys 메인 저장소 키 목록
     * @return 저장소 통계 응답
     */
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
