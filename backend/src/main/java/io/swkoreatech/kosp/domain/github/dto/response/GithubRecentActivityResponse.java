package io.swkoreatech.kosp.domain.github.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;

/**
 * GitHub 최근 기여 활동 응답 DTO.
 *
 * @param repoOwner 저장소 소유자
 * @param repositoryName 저장소 이름
 * @param description 저장소 설명
 * @param stargazersCount 스타 수
 * @param userCommitCount 사용자 커밋 수
 * @param userPrCount 사용자 PR 수
 * @param lastCommitDate 마지막 커밋 일시
 */
@Schema(description = "1. 최근 기여활동 항목")
public record GithubRecentActivityResponse(
    String repoOwner,
    String repositoryName,
    String description,
    Integer stargazersCount,
    Integer userCommitCount,
    Integer userPrCount,
    LocalDateTime lastCommitDate
) {
    /**
     * 저장소 통계로부터 최근 기여 활동 응답을 생성한다.
     *
     * @param repo 저장소 통계
     * @return 최근 기여 활동 응답
     */
    public static GithubRecentActivityResponse from(GithubRepositoryStatistics repo) {
        return new GithubRecentActivityResponse(
            repo.getRepoOwner(),
            repo.getRepoName(),
            repo.getDescription(),
            repo.getStargazersCount(),
            repo.getUserCommitsCount(),
            repo.getUserPrsCount(),
            repo.getLastCommitDate()
        );
    }
}

