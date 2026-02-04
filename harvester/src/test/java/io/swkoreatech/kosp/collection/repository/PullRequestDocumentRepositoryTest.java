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

import io.swkoreatech.kosp.collection.document.PullRequestDocument;

@DataMongoTest
@ActiveProfiles("test")
@DisplayName("PullRequestDocumentRepository 중복 검증 테스트")
class PullRequestDocumentRepositoryTest {

	@Autowired
	private PullRequestDocumentRepository repository;

	@AfterEach
	void tearDown() {
		repository.deleteAll();
	}

	@Nested
	@DisplayName("existsByUserIdAndRepositoryNameAndPrNumber 메서드")
	class ExistsByUserIdAndRepositoryNameAndPrNumberTest {

		@Test
		@DisplayName("다른 레포, 같은 PR 번호 - false 반환")
		void shouldReturnFalseForSamePrNumberDifferentRepo() {
			// Given: Save PR in repo "alice/project"
			PullRequestDocument pr = PullRequestDocument.builder()
				.userId(1L)
				.repositoryName("alice/project")
				.prNumber(42L)
				.title("Test PR")
				.state("OPEN")
				.repositoryOwner("alice")
				.additions(20)
				.deletions(10)
				.createdAt(Instant.now())
				.build();
			repository.save(pr);

			// When: Check for same PR number in different repo
			boolean exists = repository.existsByUserIdAndRepositoryNameAndPrNumber(
				1L, "bob/project", 42L);

			// Then: Should NOT exist (different repo)
			assertThat(exists).isFalse();
		}

		@Test
		@DisplayName("같은 레포, 같은 PR 번호 - true 반환")
		void shouldReturnTrueForSamePrNumber() {
			// Given: Save PR
			PullRequestDocument pr = PullRequestDocument.builder()
				.userId(1L)
				.repositoryName("alice/project")
				.prNumber(42L)
				.title("Test PR")
				.state("OPEN")
				.repositoryOwner("alice")
				.additions(20)
				.deletions(10)
				.createdAt(Instant.now())
				.build();
			repository.save(pr);

			// When: Check for same PR number in same repo
			boolean exists = repository.existsByUserIdAndRepositoryNameAndPrNumber(
				1L, "alice/project", 42L);

			// Then: Should exist (duplicate)
			assertThat(exists).isTrue();
		}
	}
}
