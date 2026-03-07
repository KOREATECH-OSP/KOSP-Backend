package io.swkoreatech.kosp.collection.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.collection.document.CollectionMetadataDocument;

/**
 * 수집 메타데이터 MongoDB 리포지토리.
 *
 * <p>사용자별 수집 메타데이터 문서에 대한 CRUD 및 조회 기능을 제공한다.
 */
public interface CollectionMetadataRepository extends MongoRepository<CollectionMetadataDocument, String> {

    /**
     * 사용자 ID로 수집 메타데이터를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 수집 메타데이터 Optional
     */
    Optional<CollectionMetadataDocument> findByUserId(Long userId);

    /**
     * 사용자 ID로 수집 메타데이터를 조회하되, 없으면 새로 생성하여 반환한다.
     *
     * @param userId 사용자 ID
     * @return 수집 메타데이터 문서
     */
    default CollectionMetadataDocument getByUserId(Long userId) {
        return findByUserId(userId)
            .orElseGet(() -> CollectionMetadataDocument.createNew(userId));
    }
}
