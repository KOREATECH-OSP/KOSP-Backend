package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "github_collection_metadata")
@Data
@NoArgsConstructor
public class GithubCollectionMetadata {
    
    @Id
    private String id;
    
    private String githubLogin;
    private String repoOwner;
    private String repoName;
    
    private String collectionType; // "commits", "issues", "prs", "events"
    
    private LocalDateTime lastCollectedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static GithubCollectionMetadata create(
        String githubLogin,
        String repoOwner,
        String repoName,
        String collectionType
    ) {
        GithubCollectionMetadata metadata = new GithubCollectionMetadata();
        metadata.githubLogin = githubLogin;
        metadata.repoOwner = repoOwner;
        metadata.repoName = repoName;
        metadata.collectionType = collectionType;
        metadata.lastCollectedAt = LocalDateTime.now();
        metadata.createdAt = LocalDateTime.now();
        metadata.updatedAt = LocalDateTime.now();
        return metadata;
    }
    
    public void updateLastCollected() {
        this.lastCollectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
