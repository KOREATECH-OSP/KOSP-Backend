package io.swkoreatech.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;
import io.swkoreatech.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserStatisticsCalculator 단위 테스트")
class UserStatisticsCalculatorTest {

    @InjectMocks
    private UserStatisticsCalculator calculator;

    @Mock
    private GithubCommitDetailRawRepository commitDetailRawRepository;

    @Mock
    private GithubRepositoryStatisticsRepository repositoryStatisticsRepository;

    @Mock
    private GithubPRIssueCountService prIssueCountService;

    private GithubCommitDetailRaw createCommit(String githubId, String date, int additions, int deletions, String repoOwner, String repoName) {
        return GithubCommitDetailRaw.builder()
            .sha("sha-" + date + "-" + repoName)
            .repoOwner(repoOwner)
            .repoName(repoName)
            .author(Map.of(
                "login", githubId,
                "date", date
            ))
            .stats(Map.of(
                "additions", additions,
                "deletions", deletions
            ))
            .build();
    }

    private GithubRepositoryStatistics createRepoStats(String githubId, String repoOwner, String repoName, int stars, int forks) {
        GithubRepositoryStatistics stats = GithubRepositoryStatistics.create(repoOwner, repoName, githubId);
        ReflectionTestUtils.setField(stats, "stargazersCount", stars);
        ReflectionTestUtils.setField(stats, "forksCount", forks);
        return stats;
    }

    @Nested
    @DisplayName("calculate 메서드")
    class CalculateTest {

        @Test
        @DisplayName("커밋이 없으면 빈 통계를 반환한다")
        void returnsEmptyStats_whenNoCommits() {
            // given
            String githubId = "testuser";
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(List.of());

            // when
            GithubUserStatistics result = calculator.calculate(githubId);

            // then
            assertThat(result.getGithubId()).isEqualTo(githubId);
        }

        @Test
        @DisplayName("사용자 통계를 계산한다")
        void calculatesUserStatistics() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50, "owner", "repo1"),
                createCommit(githubId, "2024-01-20T10:00:00Z", 200, 100, "owner", "repo2")
            );
            GithubRepositoryStatistics repoStats = createRepoStats(githubId, "owner", "repo1", 10, 5);

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of(repoStats));
            given(prIssueCountService.countUserPRs(githubId)).willReturn(5);
            given(prIssueCountService.countUserIssues(githubId)).willReturn(3);

            // when
            GithubUserStatistics result = calculator.calculate(githubId);

            // then
            assertThat(result.getTotalCommits()).isEqualTo(2);
            assertThat(result.getTotalAdditions()).isEqualTo(300);
            assertThat(result.getTotalDeletions()).isEqualTo(150);
            assertThat(result.getTotalPrs()).isEqualTo(5);
            assertThat(result.getTotalIssues()).isEqualTo(3);
        }

        @Test
        @DisplayName("야간 커밋과 주간 커밋을 분리한다")
        void separatesNightAndDayCommits() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50, "owner", "repo"),
                createCommit(githubId, "2024-01-15T23:00:00Z", 100, 50, "owner", "repo"),
                createCommit(githubId, "2024-01-16T02:00:00Z", 100, 50, "owner", "repo")
            );

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of());
            given(prIssueCountService.countUserPRs(githubId)).willReturn(0);
            given(prIssueCountService.countUserIssues(githubId)).willReturn(0);

            // when
            GithubUserStatistics result = calculator.calculate(githubId);

            // then
            assertThat(result.getNightCommits()).isEqualTo(2);
            assertThat(result.getDayCommits()).isEqualTo(1);
        }

        @Test
        @DisplayName("스타와 포크 수를 합산한다")
        void sumsStarsAndForks() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50, "owner", "repo1")
            );
            GithubRepositoryStatistics repo1 = createRepoStats(githubId, "owner", "repo1", 10, 5);
            GithubRepositoryStatistics repo2 = createRepoStats(githubId, "owner", "repo2", 20, 10);

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of(repo1, repo2));
            given(prIssueCountService.countUserPRs(githubId)).willReturn(0);
            given(prIssueCountService.countUserIssues(githubId)).willReturn(0);

            // when
            GithubUserStatistics result = calculator.calculate(githubId);

            // then
            assertThat(result.getTotalStarsReceived()).isEqualTo(30);
            assertThat(result.getTotalForksReceived()).isEqualTo(15);
        }

        @Test
        @DisplayName("상위 3개 저장소를 메인 저장소로 선정한다")
        void selectsTop3ReposAsMainRepos() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-01T10:00:00Z", 10, 5, "owner", "repo1"),
                createCommit(githubId, "2024-01-02T10:00:00Z", 10, 5, "owner", "repo1"),
                createCommit(githubId, "2024-01-03T10:00:00Z", 10, 5, "owner", "repo1"),
                createCommit(githubId, "2024-01-04T10:00:00Z", 10, 5, "owner", "repo1"),
                createCommit(githubId, "2024-01-05T10:00:00Z", 10, 5, "owner", "repo2"),
                createCommit(githubId, "2024-01-06T10:00:00Z", 10, 5, "owner", "repo2"),
                createCommit(githubId, "2024-01-07T10:00:00Z", 10, 5, "owner", "repo2"),
                createCommit(githubId, "2024-01-08T10:00:00Z", 10, 5, "owner", "repo3"),
                createCommit(githubId, "2024-01-09T10:00:00Z", 10, 5, "owner", "repo3"),
                createCommit(githubId, "2024-01-10T10:00:00Z", 10, 5, "owner", "repo4")
            );

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of());
            given(prIssueCountService.countUserPRs(githubId)).willReturn(0);
            given(prIssueCountService.countUserIssues(githubId)).willReturn(0);

            // when
            GithubUserStatistics result = calculator.calculate(githubId);

            // then
            assertThat(result.getTotalCommits()).isEqualTo(10);
        }

        @Test
        @DisplayName("소유한 저장소와 기여한 저장소를 분리한다")
        void separatesOwnedAndContributedRepos() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50, "testuser", "my-repo"),
                createCommit(githubId, "2024-01-20T10:00:00Z", 200, 100, "other", "contrib-repo")
            );
            GithubRepositoryStatistics ownedRepo = createRepoStats(githubId, "testuser", "my-repo", 10, 5);
            GithubRepositoryStatistics contributedRepo = createRepoStats(githubId, "other", "contrib-repo", 20, 10);

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId))
                .willReturn(List.of(ownedRepo, contributedRepo));
            given(prIssueCountService.countUserPRs(githubId)).willReturn(0);
            given(prIssueCountService.countUserIssues(githubId)).willReturn(0);

            // when
            GithubUserStatistics result = calculator.calculate(githubId);

            // then
            assertThat(result.getOwnedReposCount()).isEqualTo(1);
            assertThat(result.getContributedReposCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("점수를 계산하여 저장한다")
        void calculatesAndSavesScores() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 1000, 500, "owner", "main-repo")
            );
            GithubRepositoryStatistics repoStats = createRepoStats(githubId, "owner", "main-repo", 100, 50);

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of(repoStats));
            given(prIssueCountService.countUserPRs(githubId)).willReturn(10);
            given(prIssueCountService.countUserIssues(githubId)).willReturn(5);

            // when
            GithubUserStatistics result = calculator.calculate(githubId);

            // then
            assertThat(result.getMainRepoScore()).isNotNull();
            assertThat(result.getPrIssueScore()).isEqualTo(BigDecimal.valueOf(650));
            assertThat(result.getReputationScore()).isEqualTo(BigDecimal.valueOf(2000));
        }
    }
}
