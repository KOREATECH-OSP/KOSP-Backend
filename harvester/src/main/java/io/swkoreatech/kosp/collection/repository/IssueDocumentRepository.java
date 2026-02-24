package io.swkoreatech.kosp.collection.repository;

import io.swkoreatech.kosp.collection.document.IssueDocument;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface IssueDocumentRepository extends MongoRepository<IssueDocument, String> {

    List<IssueDocument> findByUserId(Long userId);

    List<IssueDocument> findByUserIdAndRepositoryName(Long userId, String repositoryName);

    boolean existsByUserIdAndIssueNumber(Long userId, Long issueNumber);

    boolean existsByUserIdAndRepositoryNameAndIssueNumber(Long userId, String repositoryName, Long issueNumber);
}
