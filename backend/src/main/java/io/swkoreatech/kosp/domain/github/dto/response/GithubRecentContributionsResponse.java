package io.swkoreatech.kosp.domain.github.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

/**
 * GitHub 최근 기여 저장소 응답 DTO.
 *
 * @param repositories 최근 기여 저장소 목록
 */
@Builder
public record GithubRecentContributionsResponse(
    List<RecentRepository> repositories
) {
    /**
     * 최근 기여 저장소 상세 정보.
     *
     * @param repoOwner 저장소 소유자
     * @param repoName 저장소 이름
     * @param stargazersCount 스타 수
     * @param userCommitsCount 사용자 커밋 수
     * @param userPrsCount 사용자 PR 수
     * @param userIssuesCount 사용자 이슈 수
     * @param lastCommitDate 마지막 커밋 일시
     * @param primaryLanguage 주요 프로그래밍 언어
     */
    @Builder
    public record RecentRepository(
        String repoOwner,
        String repoName,
        Integer stargazersCount,
        Integer userCommitsCount,
        Integer userPrsCount,
        Integer userIssuesCount,
        LocalDateTime lastCommitDate,
        String primaryLanguage
    ) {
    }
}
