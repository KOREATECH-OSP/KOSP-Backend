package io.swkoreatech.kosp.collection.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.collection.document.ContributedRepoDocument;

public interface ContributedRepoDocumentRepository extends MongoRepository<ContributedRepoDocument, String> {

    List<ContributedRepoDocument> findByUserId(Long userId);

    boolean existsByUserIdAndFullName(Long userId, String fullName);
}
