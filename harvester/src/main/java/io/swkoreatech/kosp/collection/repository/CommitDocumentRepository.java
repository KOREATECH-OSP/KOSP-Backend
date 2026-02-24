package io.swkoreatech.kosp.collection.repository;

import io.swkoreatech.kosp.collection.document.CommitDocument;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommitDocumentRepository extends MongoRepository<CommitDocument, String> {

    List<CommitDocument> findByUserId(Long userId);

    List<CommitDocument> findByUserIdAndRepositoryName(Long userId, String repositoryName);

    boolean existsByUserIdAndSha(Long userId, String sha);

    boolean existsByUserIdAndRepositoryNameAndSha(Long userId, String repositoryName, String sha);
}
