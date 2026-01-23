package io.swkoreatech.kosp.harvester.collection.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.harvester.collection.document.IssueDocument;

public interface IssueDocumentRepository extends MongoRepository<IssueDocument, String> {

    List<IssueDocument> findByUserId(Long userId);

    List<IssueDocument> findByUserIdAndRepositoryName(Long userId, String repositoryName);

    boolean existsByUserIdAndIssueNumber(Long userId, Long issueNumber);
}
