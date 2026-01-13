package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User Starred Repository
 * 
 * Reference: SKKU-OSP UserStarred (Line 297-301)
 */
@Document(collection = "github_user_starred")
@CompoundIndex(
    name = "user_starred_idx", 
    def = "{'githubId': 1, 'starredRepoOwner': 1, 'starredRepoName': 1}", 
    unique = true
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GithubUserStarred {
    
    @Id
    private String id;
    
    /**
     * 사용자 GitHub ID
     */
    private String githubId;
    
    /**
     * Starred 저장소 소유자
     */
    private String starredRepoOwner;
    
    /**
     * Starred 저장소 이름
     */
    private String starredRepoName;
    
    /**
     * 수집 시각
     */
    private LocalDateTime collectedAt;
    
    public static GithubUserStarred create(
        String githubId,
        String starredRepoOwner,
        String starredRepoName
    ) {
        return GithubUserStarred.builder()
            .githubId(githubId)
            .starredRepoOwner(starredRepoOwner)
            .starredRepoName(starredRepoName)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
