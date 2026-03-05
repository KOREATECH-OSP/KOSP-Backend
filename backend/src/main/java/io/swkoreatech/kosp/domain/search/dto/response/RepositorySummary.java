package io.swkoreatech.kosp.domain.search.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;

/**
 * 저장소 요약 정보 DTO.
 *
 * @param repoOwner 저장소 소유자
 * @param repoName 저장소 이름
 * @param description 저장소 설명
 * @param primaryLanguage 주요 프로그래밍 언어
 * @param stargazersCount 스타 수
 * @param forksCount 포크 수
 * @param lastCommitDate 마지막 커밋 일시
 */
public record RepositorySummary(
    String repoOwner,
    String repoName,
    String description,
    String primaryLanguage,
    Integer stargazersCount,
    Integer forksCount,
    LocalDateTime lastCommitDate
) {
    /**
     * 저장소 통계로부터 요약 정보를 생성한다.
     *
     * @param stats 저장소 통계
     * @return 저장소 요약 정보
     */
    public static RepositorySummary from(GithubRepositoryStatistics stats) {
        return new RepositorySummary(
            stats.getRepoOwner(),
            stats.getRepoName(),
            stats.getDescription(),
            stats.getPrimaryLanguage(),
            stats.getStargazersCount(),
            stats.getForksCount(),
            stats.getLastCommitDate()
        );
    }
}
