package io.swkoreatech.kosp.collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.ActiveProfiles;

import io.swkoreatech.kosp.collection.document.CommitDocument;
import io.swkoreatech.kosp.collection.document.IssueDocument;
import io.swkoreatech.kosp.collection.document.PullRequestDocument;
import io.swkoreatech.kosp.collection.repository.CommitDocumentRepository;
import io.swkoreatech.kosp.collection.repository.IssueDocumentRepository;
import io.swkoreatech.kosp.collection.repository.PullRequestDocumentRepository;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MongoDB Unique Index Integration Test")
class MongoIndexIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CommitDocumentRepository commitRepository;

    @Autowired
    private PullRequestDocumentRepository prRepository;

    @Autowired
    private IssueDocumentRepository issueRepository;

    @BeforeEach
    void setUp() {
        mongoTemplate.createCollection(CommitDocument.class);
        mongoTemplate.createCollection(PullRequestDocument.class);
        mongoTemplate.createCollection(IssueDocument.class);
        
        mongoTemplate.indexOps(CommitDocument.class).createIndex(
            new org.springframework.data.mongodb.core.index.CompoundIndexDefinition(
                new org.bson.Document("userId", 1)
                    .append("repositoryName", 1)
                    .append("sha", 1)
            ).unique().named("unique_commit_idx")
        );
        
        mongoTemplate.indexOps(PullRequestDocument.class).createIndex(
            new org.springframework.data.mongodb.core.index.CompoundIndexDefinition(
                new org.bson.Document("userId", 1)
                    .append("repositoryName", 1)
                    .append("prNumber", 1)
            ).unique().named("unique_pr_idx")
        );
        
        mongoTemplate.indexOps(IssueDocument.class).createIndex(
            new org.springframework.data.mongodb.core.index.CompoundIndexDefinition(
                new org.bson.Document("userId", 1)
                    .append("repositoryName", 1)
                    .append("issueNumber", 1)
            ).unique().named("unique_issue_idx")
        );
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection("github_commits");
        mongoTemplate.dropCollection("github_pull_requests");
        mongoTemplate.dropCollection("github_issues");
    }

    @Nested
    @DisplayName("Unique Index Existence Tests")
    class UniqueIndexExistenceTest {

        @Test
        @DisplayName("Commit collection should have unique_commit_idx")
        void shouldHaveUniqueCommitIndex() {
            // when
            List<IndexInfo> indexes = mongoTemplate.indexOps("github_commits").getIndexInfo();

            // then
            IndexInfo uniqueIndex = indexes.stream()
                .filter(idx -> idx.getName().equals("unique_commit_idx"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("unique_commit_idx not found"));

            assertThat(uniqueIndex.isUnique()).isTrue();
        }

        @Test
        @DisplayName("Pull Request collection should have unique_pr_idx")
        void shouldHaveUniquePullRequestIndex() {
            // when
            List<IndexInfo> indexes = mongoTemplate.indexOps("github_pull_requests").getIndexInfo();

            // then
            IndexInfo uniqueIndex = indexes.stream()
                .filter(idx -> idx.getName().equals("unique_pr_idx"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("unique_pr_idx not found"));

            assertThat(uniqueIndex.isUnique()).isTrue();
        }

        @Test
        @DisplayName("Issue collection should have unique_issue_idx")
        void shouldHaveUniqueIssueIndex() {
            // when
            List<IndexInfo> indexes = mongoTemplate.indexOps("github_issues").getIndexInfo();

            // then
            IndexInfo uniqueIndex = indexes.stream()
                .filter(idx -> idx.getName().equals("unique_issue_idx"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("unique_issue_idx not found"));

            assertThat(uniqueIndex.isUnique()).isTrue();
        }
    }

    @Nested
    @DisplayName("Duplicate Insert Prevention Tests")
    class DuplicateInsertPreventionTest {

        @Test
        @DisplayName("Should throw DuplicateKeyException on duplicate commit")
        void shouldPreventDuplicateCommit() {
            // given
            Long userId = 1L;
            String repositoryName = "test-repo";
            String sha = "abc123def456";

            CommitDocument commit = CommitDocument.builder()
                .userId(userId)
                .repositoryName(repositoryName)
                .sha(sha)
                .message("Test commit")
                .repositoryOwner("test-owner")
                .authorName("Test Author")
                .authorEmail("test@example.com")
                .authoredAt(Instant.now())
                .additions(10)
                .deletions(5)
                .build();

            commitRepository.save(commit);

            // when & then
            CommitDocument duplicateCommit = CommitDocument.builder()
                .userId(userId)
                .repositoryName(repositoryName)
                .sha(sha)
                .message("Duplicate commit")
                .repositoryOwner("test-owner")
                .authorName("Test Author")
                .authorEmail("test@example.com")
                .authoredAt(Instant.now())
                .additions(10)
                .deletions(5)
                .build();

            assertThatThrownBy(() -> commitRepository.save(duplicateCommit))
                .isInstanceOf(DuplicateKeyException.class);
        }

        @Test
        @DisplayName("Should throw DuplicateKeyException on duplicate pull request")
        void shouldPreventDuplicatePullRequest() {
            // given
            Long userId = 2L;
            String repositoryName = "test-repo-pr";
            Long prNumber = 42L;

            PullRequestDocument pr = PullRequestDocument.builder()
                .userId(userId)
                .repositoryName(repositoryName)
                .prNumber(prNumber)
                .title("Test PR")
                .state("OPEN")
                .repositoryOwner("test-owner")
                .additions(20)
                .deletions(10)
                .createdAt(Instant.now())
                .build();

            prRepository.save(pr);

            // when & then
            PullRequestDocument duplicatePr = PullRequestDocument.builder()
                .userId(userId)
                .repositoryName(repositoryName)
                .prNumber(prNumber)
                .title("Duplicate PR")
                .state("OPEN")
                .repositoryOwner("test-owner")
                .additions(20)
                .deletions(10)
                .createdAt(Instant.now())
                .build();

            assertThatThrownBy(() -> prRepository.save(duplicatePr))
                .isInstanceOf(DuplicateKeyException.class);
        }

        @Test
        @DisplayName("Should throw DuplicateKeyException on duplicate issue")
        void shouldPreventDuplicateIssue() {
            // given
            Long userId = 3L;
            String repositoryName = "test-repo-issue";
            Long issueNumber = 99L;

            IssueDocument issue = IssueDocument.builder()
                .userId(userId)
                .repositoryName(repositoryName)
                .issueNumber(issueNumber)
                .title("Test Issue")
                .state("OPEN")
                .repositoryOwner("test-owner")
                .commentsCount(5)
                .createdAt(Instant.now())
                .build();

            issueRepository.save(issue);

            // when & then
            IssueDocument duplicateIssue = IssueDocument.builder()
                .userId(userId)
                .repositoryName(repositoryName)
                .issueNumber(issueNumber)
                .title("Duplicate Issue")
                .state("OPEN")
                .repositoryOwner("test-owner")
                .commentsCount(5)
                .createdAt(Instant.now())
                .build();

            assertThatThrownBy(() -> issueRepository.save(duplicateIssue))
                .isInstanceOf(DuplicateKeyException.class);
        }
    }
}
