package kr.ac.koreatech.sw.kosp.domain.github.mongo.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "github_repositories")
public class GithubRepository {

    @Id
    private String id; // generated or compound

    @Indexed
    private Long ownerId; // GithubUser.githubId

    private String name;
    private String url;
    private String description;
    private String homepageUrl;
    private boolean isFork;

    private String primaryLanguage;
    private Map<String, Long> languages; // Language Name -> Byte Size

    private RepositoryStats stats;
    private CodeVolume codeVolume;
    private RepoDates dates;

    @Getter
    @Builder
    public static class RepositoryStats {
        private long diskUsage; // KB
        private int stargazersCount;
        private int forksCount;
        private int watchersCount;
        private int openIssuesCount;
        private int openPrsCount;
    }

    @Getter
    @Builder
    public static class CodeVolume {
        private int totalCommits;
        private long totalAdditions;
        private long totalDeletions;
    }

    @Getter
    @Builder
    public static class RepoDates {
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime pushedAt;
    }
}
