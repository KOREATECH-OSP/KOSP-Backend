package io.swkoreatech.kosp.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document(collection = "github_collection_metadata")
public class CollectionMetadataDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long userId;

    private Instant lastFullCollection;
    private Instant lastIncrementalCollection;

    private String lastCommitCursor;
    private String lastPrCursor;
    private String lastIssueCursor;

    private Instant createdAt;
    private Instant updatedAt;

    public static CollectionMetadataDocument createNew(Long userId) {
        Instant now = Instant.now();
        return CollectionMetadataDocument.builder()
            .userId(userId)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }
}
