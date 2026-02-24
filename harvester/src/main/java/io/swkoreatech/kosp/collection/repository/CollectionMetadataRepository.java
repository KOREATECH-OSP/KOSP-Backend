package io.swkoreatech.kosp.collection.repository;

import io.swkoreatech.kosp.collection.document.CollectionMetadataDocument;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CollectionMetadataRepository extends MongoRepository<CollectionMetadataDocument, String> {

    Optional<CollectionMetadataDocument> findByUserId(Long userId);

    default CollectionMetadataDocument getByUserId(Long userId) {
        return findByUserId(userId)
            .orElseGet(() -> CollectionMetadataDocument.createNew(userId));
    }
}
