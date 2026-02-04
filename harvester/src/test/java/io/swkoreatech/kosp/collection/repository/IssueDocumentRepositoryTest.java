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

import io.swkoreatech.kosp.collection.document.IssueDocument;

@DataMongoTest
@ActiveProfiles("test")
@DisplayName("IssueDocumentRepository 중복 검증 테스트")
class IssueDocumentRepositoryTest {

	@Autowired
	private IssueDocumentRepository repository;

	@AfterEach
	void tearDown() {
		repository.deleteAll();
	}

	@Nested
	@DisplayName("existsByUserIdAndRepositoryNameAndIssueNumber 메서드")
	class ExistsByUserIdAndRepositoryNameAndIssueNumberTest {

		@Test
		@DisplayName("다른 레포, 같은 이슈 번호 - false 반환")
		void shouldReturnFalseForSameIssueNumberDifferentRepo() {
			// Given: Save issue in repo "alice/project"
			IssueDocument issue = IssueDocument.builder()
				.userId(1L)
				.repositoryName("alice/project")
				.issueNumber(99L)
				.title("Test Issue")
				.state("OPEN")
				.repositoryOwner("alice")
				.commentsCount(5)
				.createdAt(Instant.now())
				.build();
			repository.save(issue);

			// When: Check for same issue number in different repo
			boolean exists = repository.existsByUserIdAndRepositoryNameAndIssueNumber(
				1L, "bob/project", 99L);

			// Then: Should NOT exist (different repo)
			assertThat(exists).isFalse();
		}

		@Test
		@DisplayName("같은 레포, 같은 이슈 번호 - true 반환")
		void shouldReturnTrueForSameIssueNumber() {
			// Given: Save issue
			IssueDocument issue = IssueDocument.builder()
				.userId(1L)
				.repositoryName("alice/project")
				.issueNumber(99L)
				.title("Test Issue")
				.state("OPEN")
				.repositoryOwner("alice")
				.commentsCount(5)
				.createdAt(Instant.now())
				.build();
			repository.save(issue);

			// When: Check for same issue number in same repo
			boolean exists = repository.existsByUserIdAndRepositoryNameAndIssueNumber(
				1L, "alice/project", 99L);

			// Then: Should exist (duplicate)
			assertThat(exists).isTrue();
		}
	}
}
