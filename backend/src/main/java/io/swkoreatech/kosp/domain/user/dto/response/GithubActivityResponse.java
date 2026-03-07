package io.swkoreatech.kosp.domain.user.dto.response;

import java.util.Collections;
import java.util.List;

/**
 * GitHub 활동 응답 DTO.
 *
 * @param activities GitHub 활동 목록
 */
public record GithubActivityResponse(
    List<Activity> activities
) {
    /**
     * 빈 GitHub 활동 응답을 생성한다.
     *
     * @return 빈 활동 목록을 가진 응답
     */
    public static GithubActivityResponse empty() {
        return new GithubActivityResponse(Collections.emptyList());
    }

    /**
     * GitHub 활동 항목.
     *
     * @param id 활동 ID (레포지토리 ID 또는 이벤트 ID)
     * @param type 활동 유형 (REPOSITORY, PR, ISSUE)
     * @param repoName 레포지토리 이름
     * @param title 제목 (레포지토리 설명 또는 이벤트 내용)
     * @param date 활동 일시
     * @param url 활동 URL
     */
    public record Activity(
        String id, // Repo ID or Event ID
        String type, // "REPOSITORY", "PR", "ISSUE"
        String repoName,
        String title, // Repo description or Event payload
        String date, // LocalDateTime string
        String url
    ) {
    }
}
