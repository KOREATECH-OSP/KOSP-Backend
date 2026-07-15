package io.swkoreatech.kosp.domain.github.dto.response;

import java.time.format.DateTimeFormatter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;

/**
 * GitHub 저장소 → 이력서 프로젝트 가져오기 응답 DTO.
 *
 * <p>프론트 이력서 편집기의 프로젝트 항목(ProjectItem)에 그대로 채워 넣을 수 있도록
 * 자동 채움 필드를 미리 가공해 제공한다. {@code repoKey}(owner/repo 소문자)는 기존
 * 이력서 프로젝트의 githubLink 와 중복 판별하는 키로 사용한다.</p>
 *
 * @param repoKey          중복 판별 키 (owner/repo, 소문자)
 * @param name             프로젝트명 (저장소 이름)
 * @param githubLink       GitHub 링크
 * @param summary          요약 (저장소 설명)
 * @param techStack        주요 언어 (프론트에서 배열로 변환)
 * @param period           기간 문자열 (예: "2024.03 ~ 2025.01")
 * @param myContributions  내 기여 요약 (커밋/PR/이슈)
 * @param result           성과 요약 (스타/포크)
 * @param isOwned          소유 저장소 여부 (대표 프로젝트 추천 판단용)
 * @param stargazersCount  스타 수
 * @param userCommitsCount 내 커밋 수
 * @param userPrsCount     내 PR 수
 * @param userIssuesCount  내 이슈 수
 * @param primaryLanguage  주요 언어 원본값
 */
@Schema(description = "GitHub 저장소 → 이력서 프로젝트 가져오기 항목")
public record GithubResumeProjectResponse(
    String repoKey,
    String name,
    String githubLink,
    String summary,
    String techStack,
    String period,
    String myContributions,
    String result,
    Boolean isOwned,
    Integer stargazersCount,
    Integer userCommitsCount,
    Integer userPrsCount,
    Integer userIssuesCount,
    String primaryLanguage
) {

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM");

    public static GithubResumeProjectResponse from(GithubRepositoryStatistics repo) {
        String owner = repo.getRepoOwner();
        String name = repo.getRepoName();
        String repoKey = (owner + "/" + name).toLowerCase();
        String githubLink = "https://github.com/" + owner + "/" + name;

        return new GithubResumeProjectResponse(
            repoKey,
            name,
            githubLink,
            repo.getDescription(),
            repo.getPrimaryLanguage(),
            buildPeriod(repo),
            buildContributions(repo),
            buildResult(repo),
            repo.getIsOwned(),
            repo.getStargazersCount(),
            repo.getUserCommitsCount(),
            repo.getUserPrsCount(),
            repo.getUserIssuesCount(),
            repo.getPrimaryLanguage()
        );
    }

    private static String buildPeriod(GithubRepositoryStatistics repo) {
        String start = repo.getRepoCreatedAt() == null ? null : repo.getRepoCreatedAt().format(PERIOD_FORMAT);
        String end = repo.getLastCommitDate() == null ? null : repo.getLastCommitDate().format(PERIOD_FORMAT);
        if (start == null && end == null) {
            return null;
        }
        if (start == null) {
            return end;
        }
        if (end == null) {
            return start;
        }
        return start.equals(end) ? start : start + " ~ " + end;
    }

    private static String buildContributions(GithubRepositoryStatistics repo) {
        return "커밋 %d · PR %d · 이슈 %d".formatted(
            nz(repo.getUserCommitsCount()),
            nz(repo.getUserPrsCount()),
            nz(repo.getUserIssuesCount())
        );
    }

    private static String buildResult(GithubRepositoryStatistics repo) {
        return "⭐ %d · 🍴 %d".formatted(nz(repo.getStargazersCount()), nz(repo.getForksCount()));
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
