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
 * 사용자-저장소 기여 관계
 * 
 * Reference: SKKU-OSP RepoContribute (Line 241-252)
 */
@Document(collection = "github_repo_contribute")
@CompoundIndex(name = "user_repo_idx", def = "{'githubId': 1, 'ownerId': 1, 'repoName': 1}", unique = true)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GithubRepoContribute {
    
    @Id
    private String id;
    
    /**
     * 사용자 GitHub ID
     */
    private String githubId;
    
    /**
     * 저장소 소유자
     */
    private String ownerId;
    
    /**
     * 저장소 이름
     */
    private String repoName;
    
    /**
     * 소유 저장소 여부
     * true: 사용자가 소유
     * false: 기여만 함
     */
    private Boolean isOwned;
    
    /**
     * 수집 시각
     */
    private LocalDateTime collectedAt;
    
    public static GithubRepoContribute create(
        String githubId,
        String ownerId,
        String repoName,
        Boolean isOwned
    ) {
        return GithubRepoContribute.builder()
            .githubId(githubId)
            .ownerId(ownerId)
            .repoName(repoName)
            .isOwned(isOwned)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
