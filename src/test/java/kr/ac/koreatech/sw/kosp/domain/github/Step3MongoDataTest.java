package kr.ac.koreatech.sw.kosp.domain.github;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserBasicRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubUserBasicRawRepository;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Step 3: MongoDB Raw Data 수집 테스트")
class Step3MongoDataTest {

    @Autowired(required = false)
    private GithubCommitDetailRawRepository commitDetailRawRepository;

    @Autowired(required = false)
    private GithubUserBasicRawRepository userBasicRawRepository;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        // MongoDB 데이터 정리
        if (mongoTemplate != null) {
            mongoTemplate.dropCollection(GithubCommitDetailRaw.class);
            mongoTemplate.dropCollection(GithubUserBasicRaw.class);
        }
    }

    @Test
    @DisplayName("MongoDB Repository 빈 로드 테스트")
    void testMongoRepositoryBeans() {
        assertThat(commitDetailRawRepository).isNotNull();
        assertThat(userBasicRawRepository).isNotNull();
    }

    @Test
    @DisplayName("GithubCommitDetailRaw 저장 및 조회 테스트")
    void testCommitDetailRawSaveAndFind() {
        // Given
        String sha = "test-sha-12345";
        Map<String, Object> author = Map.of(
            "login", "testuser",
            "name", "Test User",
            "email", "test@example.com",
            "date", "2024-01-01T00:00:00Z"
        );
        Map<String, Object> stats = Map.of(
            "additions", 100,
            "deletions", 50,
            "total", 150
        );

        GithubCommitDetailRaw raw = GithubCommitDetailRaw.create(
            sha,
            "owner",
            "repo",
            author,
            author,
            stats,
            List.of(),
            "Test commit message"
        );

        // When
        GithubCommitDetailRaw saved = commitDetailRawRepository.save(raw);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSha()).isEqualTo(sha);
        assertThat(saved.getAdditions()).isEqualTo(100);
        assertThat(saved.getDeletions()).isEqualTo(50);
        assertThat(saved.getAuthorLogin()).isEqualTo("testuser");

        // 조회 테스트
        GithubCommitDetailRaw found = commitDetailRawRepository.findBySha(sha).orElseThrow();
        assertThat(found.getSha()).isEqualTo(sha);
        assertThat(found.getRepoOwner()).isEqualTo("owner");
        assertThat(found.getRepoName()).isEqualTo("repo");
    }

    @Test
    @DisplayName("GithubCommitDetailRaw SHA 중복 체크 테스트")
    void testCommitDetailRawDuplicateCheck() {
        // Given
        String sha = "duplicate-test-sha";
        GithubCommitDetailRaw raw = GithubCommitDetailRaw.create(
            sha,
            "owner",
            "repo",
            Map.of("login", "user"),
            Map.of("login", "user"),
            Map.of("additions", 10, "deletions", 5),
            List.of(),
            "Message"
        );

        // When
        commitDetailRawRepository.save(raw);

        // Then
        assertThat(commitDetailRawRepository.existsBySha(sha)).isTrue();
        assertThat(commitDetailRawRepository.existsBySha("non-existent-sha")).isFalse();
    }

    @Test
    @DisplayName("GithubUserBasicRaw 저장 및 조회 테스트")
    void testUserBasicRawSaveAndFind() {
        // Given
        String githubId = "testuser123";
        GithubUserBasicRaw raw = GithubUserBasicRaw.create(
            githubId,
            "Test User",
            "https://avatar.url",
            "Test bio",
            "Test Company",
            "Seoul",
            "test@example.com",
            "2020-01-01T00:00:00Z",
            100,
            50,
            20,
            List.of(),
            Map.of("totalCommitContributions", 500),
            0
        );

        // When
        GithubUserBasicRaw saved = userBasicRawRepository.save(raw);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getGithubId()).isEqualTo(githubId);
        assertThat(saved.getName()).isEqualTo("Test User");
        assertThat(saved.getFollowersCount()).isEqualTo(100);
        assertThat(saved.getTotalRepositories()).isEqualTo(20);

        // 조회 테스트
        GithubUserBasicRaw found = userBasicRawRepository.findByGithubId(githubId).orElseThrow();
        assertThat(found.getGithubId()).isEqualTo(githubId);
        assertThat(found.getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("레포지토리별 커밋 조회 테스트")
    void testFindCommitsByRepository() {
        // Given
        String repoOwner = "test-owner";
        String repoName = "test-repo";

        GithubCommitDetailRaw commit1 = GithubCommitDetailRaw.create(
            "sha1", repoOwner, repoName,
            Map.of("login", "user1"),
            Map.of("login", "user1"),
            Map.of("additions", 10, "deletions", 5),
            List.of(), "Commit 1"
        );

        GithubCommitDetailRaw commit2 = GithubCommitDetailRaw.create(
            "sha2", repoOwner, repoName,
            Map.of("login", "user2"),
            Map.of("login", "user2"),
            Map.of("additions", 20, "deletions", 10),
            List.of(), "Commit 2"
        );

        commitDetailRawRepository.save(commit1);
        commitDetailRawRepository.save(commit2);

        // When
        List<GithubCommitDetailRaw> commits = commitDetailRawRepository
            .findByRepoOwnerAndRepoName(repoOwner, repoName);

        // Then
        assertThat(commits).hasSize(2);
        assertThat(commits).extracting(GithubCommitDetailRaw::getSha)
            .containsExactlyInAnyOrder("sha1", "sha2");
    }
}
