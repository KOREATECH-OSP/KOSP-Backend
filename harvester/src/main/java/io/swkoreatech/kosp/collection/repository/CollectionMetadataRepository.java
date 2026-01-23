package io.swkoreatech.kosp.collection.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.collection.document.CollectionMetadataDocument;

public interface CollectionMetadataRepository extends MongoRepository<CollectionMetadataDocument, String> {

    Optional<CollectionMetadataDocument> findByUserId(Long userId);

    default CollectionMetadataDocument getByUserId(Long userId) {
        return findByUserId(userId)
            .orElseGet(() -> CollectionMetadataDocument.createNew(userId));
    }
}
