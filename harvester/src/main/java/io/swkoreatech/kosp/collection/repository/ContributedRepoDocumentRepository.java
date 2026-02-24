package io.swkoreatech.kosp.collection.repository;

import io.swkoreatech.kosp.collection.document.ContributedRepoDocument;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContributedRepoDocumentRepository extends MongoRepository<ContributedRepoDocument, String> {

    List<ContributedRepoDocument> findByUserId(Long userId);

    boolean existsByUserIdAndFullName(Long userId, String fullName);
}
