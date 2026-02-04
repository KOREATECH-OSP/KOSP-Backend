package io.swkoreatech.kosp.collection.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import io.swkoreatech.kosp.collection.document.CommitDocument;

@DataMongoTest
@ActiveProfiles("test")
@DisplayName("CommitDocumentRepository 중복 검증 테스트")
class CommitDocumentRepositoryTest {

	@Autowired
	private CommitDocumentRepository repository;

	@AfterEach
	void tearDown() {
		repository.deleteAll();
	}

	@Nested
	@DisplayName("existsByUserIdAndRepositoryNameAndSha 메서드")
	class ExistsByUserIdAndRepositoryNameAndShaTest {

		@Test
		@DisplayName("다른 레포, 같은 SHA - false 반환")
		void shouldReturnFalseForSameShaDifferentRepo() {
			// Given: Save commit in repo "alice/project"
			CommitDocument commit = CommitDocument.builder()
				.userId(1L)
				.repositoryName("alice/project")
				.sha("abc123")
				.message("Initial commit")
				.repositoryOwner("alice")
				.authorName("Alice")
				.authorEmail("alice@example.com")
				.authoredAt(Instant.now())
				.additions(10)
				.deletions(5)
				.build();
			repository.save(commit);

			// When: Check for same SHA in different repo
			boolean exists = repository.existsByUserIdAndRepositoryNameAndSha(
				1L, "bob/project", "abc123");

			// Then: Should NOT exist (different repo)
			assertThat(exists).isFalse();
		}

		@Test
		@DisplayName("같은 레포, 같은 SHA - true 반환")
		void shouldReturnTrueForSameSha() {
			// Given: Save commit
			CommitDocument commit = CommitDocument.builder()
				.userId(1L)
				.repositoryName("alice/project")
				.sha("abc123")
				.message("Initial commit")
				.repositoryOwner("alice")
				.authorName("Alice")
				.authorEmail("alice@example.com")
				.authoredAt(Instant.now())
				.additions(10)
				.deletions(5)
				.build();
			repository.save(commit);

			// When: Check for same SHA in same repo
			boolean exists = repository.existsByUserIdAndRepositoryNameAndSha(
				1L, "alice/project", "abc123");

			// Then: Should exist (duplicate)
			assertThat(exists).isTrue();
		}
	}
}
