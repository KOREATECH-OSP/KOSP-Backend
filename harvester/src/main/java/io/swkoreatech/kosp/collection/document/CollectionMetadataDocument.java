package io.swkoreatech.kosp.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

/**
 * GitHub 데이터 수집 메타데이터를 저장하는 MongoDB 문서.
 *
 * <p>사용자별 마지막 수집 시각, 커서 정보 등을 관리하여
 * 증분 수집(incremental collection)을 지원한다.
 */
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

    /**
     * 지정된 사용자에 대한 새로운 수집 메타데이터 문서를 생성한다.
     *
     * @param userId 사용자 ID
     * @return 초기화된 수집 메타데이터 문서
     */
    public static CollectionMetadataDocument createNew(Long userId) {
        Instant now = Instant.now();
        return CollectionMetadataDocument.builder()
            .userId(userId)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }
}
