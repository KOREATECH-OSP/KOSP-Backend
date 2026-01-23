package io.swkoreatech.kosp.collection.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.collection.document.PullRequestDocument;

public interface PullRequestDocumentRepository extends MongoRepository<PullRequestDocument, String> {

    List<PullRequestDocument> findByUserId(Long userId);

    List<PullRequestDocument> findByUserIdAndRepositoryName(Long userId, String repositoryName);

    boolean existsByUserIdAndPrNumber(Long userId, Long prNumber);
}
