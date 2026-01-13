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
 * User Following 관계
 * 
 * Reference: SKKU-OSP UserFollowing (Line 277-280)
 */
@Document(collection = "github_user_following")
@CompoundIndex(name = "user_following_idx", def = "{'githubId': 1, 'followingId': 1}", unique = true)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GithubUserFollowing {
    
    @Id
    private String id;
    
    /**
     * 사용자 GitHub ID
     */
    private String githubId;
    
    /**
     * Following 대상 GitHub ID
     */
    private String followingId;
    
    /**
     * 수집 시각
     */
    private LocalDateTime collectedAt;
    
    public static GithubUserFollowing create(String githubId, String followingId) {
        return GithubUserFollowing.builder()
            .githubId(githubId)
            .followingId(followingId)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
