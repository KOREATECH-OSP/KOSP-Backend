package kr.ac.koreatech.sw.kosp.domain.github.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_collection_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubCollectionMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String githubId;

    @Column(nullable = false)
    private Boolean initialCollected = false;

    private LocalDateTime lastCollectedAt;

    @Column(length = 40)
    private String lastCommitSha;

    @Column(nullable = false)
    private Integer totalApiCalls = 0;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    private LocalDateTime lastErrorAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static GithubCollectionMetadata create(String githubId) {
        GithubCollectionMetadata metadata = new GithubCollectionMetadata();
        metadata.githubId = githubId;
        return metadata;
    }

    public void markInitialCollected() {
        this.initialCollected = true;
        this.lastCollectedAt = LocalDateTime.now();
    }

    public void updateLastCollected(String lastCommitSha) {
        this.lastCollectedAt = LocalDateTime.now();
        this.lastCommitSha = lastCommitSha;
    }

    public void incrementApiCalls(int count) {
        this.totalApiCalls += count;
    }

    public void recordError(String error) {
        this.lastError = error;
        this.lastErrorAt = LocalDateTime.now();
    }
}
