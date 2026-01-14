package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document(collection = "github_commit_details_raw")
@Getter
@Builder
public class GithubCommitDetailRaw {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sha;

    private String repoOwner;
    private String repoName;

    // Author 정보
    private Map<String, Object> author;  // login, name, email, date

    // Committer 정보
    private Map<String, Object> committer;  // login, name, email, date

    // 커밋 통계 (필수!)
    private Map<String, Object> stats;  // additions, deletions, total

    // 파일 변경 정보
    private Object files;  // 배열 형태로 저장

    // 커밋 메시지
    private String message;

    // 메타데이터
    private LocalDateTime collectedAt;

    public static GithubCommitDetailRaw create(
        String sha,
        String repoOwner,
        String repoName,
        Map<String, Object> author,
        Map<String, Object> committer,
        Map<String, Object> stats,
        Object files,
        String message
    ) {
        return GithubCommitDetailRaw.builder()
            .sha(sha)
            .repoOwner(repoOwner)
            .repoName(repoName)
            .author(author)
            .committer(committer)
            .stats(stats)
            .files(files)
            .message(message)
            .collectedAt(LocalDateTime.now())
            .build();
    }

    public Integer getAdditions() {
        if (stats == null) return 0;
        Object additions = stats.get("additions");
        return additions instanceof Integer ? (Integer) additions : 0;
    }

    public Integer getDeletions() {
        if (stats == null) return 0;
        Object deletions = stats.get("deletions");
        return deletions instanceof Integer ? (Integer) deletions : 0;
    }

    public String getAuthorLogin() {
        if (author == null) return null;
        Object login = author.get("login");
        return login != null ? login.toString() : null;
    }

    public LocalDateTime getAuthorDate() {
        if (author == null) return null;
        Object date = author.get("date");
        if (date instanceof String) {
            try {
                return java.time.Instant.parse((String) date)
                    .atZone(java.time.ZoneId.of("UTC"))
                    .toLocalDateTime();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
