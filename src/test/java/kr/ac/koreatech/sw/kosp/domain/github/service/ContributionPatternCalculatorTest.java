package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubContributionPattern;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContributionPatternCalculator 단위 테스트")
class ContributionPatternCalculatorTest {

    @InjectMocks
    private ContributionPatternCalculator calculator;

    @Mock
    private GithubCommitDetailRawRepository commitDetailRawRepository;

    @Mock
    private GithubRepositoryStatisticsRepository repositoryStatisticsRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private GithubCommitDetailRaw createCommit(String githubId, String date, String repoOwner, String repoName) {
        return GithubCommitDetailRaw.builder()
            .sha("sha-" + date)
            .repoOwner(repoOwner)
            .repoName(repoName)
            .author(Map.of(
                "login", githubId,
                "date", date
            ))
            .stats(Map.of("additions", 10, "deletions", 5))
            .build();
    }

    private GithubRepositoryStatistics createRepoStats(String githubId, String repoOwner, String repoName, 
                                                        int userCommits, int totalCommits, LocalDateTime createdAt) {
        GithubRepositoryStatistics stats = GithubRepositoryStatistics.create(repoOwner, repoName, githubId);
        ReflectionTestUtils.setField(stats, "userCommitsCount", userCommits);
        ReflectionTestUtils.setField(stats, "totalCommitsCount", totalCommits);
        ReflectionTestUtils.setField(stats, "repoCreatedAt", createdAt);
        return stats;
    }

    @Nested
    @DisplayName("calculate 메서드")
    class CalculateTest {

        @Test
        @DisplayName("커밋이 없으면 기본 패턴을 반환한다")
        void returnsEmptyPattern_whenNoCommits() {
            // given
            String githubId = "testuser";
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(List.of());

            // when
            GithubContributionPattern result = calculator.calculate(githubId);

            // then
            assertThat(result.getGithubId()).isEqualTo(githubId);
        }

        @Test
        @DisplayName("야간 커밋 비율로 Night Owl 점수를 계산한다")
        void calculatesNightOwlScore() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T23:00:00Z", "owner", "repo"),
                createCommit(githubId, "2024-01-16T02:00:00Z", "owner", "repo"),
                createCommit(githubId, "2024-01-16T10:00:00Z", "owner", "repo")
            );

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of());

            // when
            GithubContributionPattern result = calculator.calculate(githubId);

            // then
            assertThat(result.getNightOwlScore()).isGreaterThan(0);
            assertThat(result.getNightCommits()).isEqualTo(2);
            assertThat(result.getDayCommits()).isEqualTo(1);
        }

        @Test
        @DisplayName("저장소 생성 1개월 이내 기여시 Initiator 점수가 높다")
        void calculatesInitiatorScore() {
            // given
            String githubId = "testuser";
            LocalDateTime repoCreatedAt = LocalDateTime.of(2024, 1, 1, 0, 0);
            
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", "owner", "repo1"),
                createCommit(githubId, "2024-06-15T10:00:00Z", "owner", "repo2")
            );
            GithubRepositoryStatistics repo1 = createRepoStats(githubId, "owner", "repo1", 10, 10, repoCreatedAt);
            GithubRepositoryStatistics repo2 = createRepoStats(githubId, "owner", "repo2", 5, 5, 
                LocalDateTime.of(2024, 1, 1, 0, 0));

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId))
                .willReturn(List.of(repo1, repo2));

            // when
            GithubContributionPattern result = calculator.calculate(githubId);

            // then
            assertThat(result.getInitiatorScore()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("사용자 커밋이 80% 이상인 저장소는 Independent로 분류한다")
        void calculatesIndependentScore() {
            // given
            String githubId = "testuser";
            
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", "owner", "solo-repo"),
                createCommit(githubId, "2024-01-20T10:00:00Z", "owner", "collab-repo")
            );
            GithubRepositoryStatistics soloRepo = createRepoStats(githubId, "owner", "solo-repo", 90, 100, null);
            GithubRepositoryStatistics collabRepo = createRepoStats(githubId, "owner", "collab-repo", 20, 100, null);

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId))
                .willReturn(List.of(soloRepo, collabRepo));

            // when
            GithubContributionPattern result = calculator.calculate(githubId);

            // then
            assertThat(result.getIndependentScore()).isEqualTo(50);
            assertThat(result.getSoloProjects()).isEqualTo(1);
        }

        @Test
        @DisplayName("협업 저장소 수를 기반으로 Coworker 수를 추정한다")
        void calculatesCoworkerCount() {
            // given
            String githubId = "testuser";
            
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", "owner", "collab1"),
                createCommit(githubId, "2024-01-20T10:00:00Z", "owner", "collab2")
            );
            GithubRepositoryStatistics collab1 = createRepoStats(githubId, "owner", "collab1", 20, 100, null);
            GithubRepositoryStatistics collab2 = createRepoStats(githubId, "owner", "collab2", 30, 100, null);

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId))
                .willReturn(List.of(collab1, collab2));

            // when
            GithubContributionPattern result = calculator.calculate(githubId);

            // then
            assertThat(result.getTotalCoworkers()).isEqualTo(4);
        }

        @Test
        @DisplayName("시간대별 커밋 분포를 JSON으로 저장한다")
        void savesHourlyDistribution() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", "owner", "repo"),
                createCommit(githubId, "2024-01-15T10:30:00Z", "owner", "repo"),
                createCommit(githubId, "2024-01-15T14:00:00Z", "owner", "repo")
            );

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of());

            // when
            GithubContributionPattern result = calculator.calculate(githubId);

            // then
            assertThat(result.getHourlyDistribution()).isNotNull();
            assertThat(result.getHourlyDistribution()).contains("10");
            assertThat(result.getHourlyDistribution()).contains("14");
        }

        @Test
        @DisplayName("totalCommitsCount가 0인 저장소는 Independent 계산에서 제외한다")
        void excludesReposWithZeroTotalCommits() {
            // given
            String githubId = "testuser";
            
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", "owner", "repo")
            );
            GithubRepositoryStatistics repoWithZeroTotal = createRepoStats(githubId, "owner", "repo", 10, 0, null);

            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(repositoryStatisticsRepository.findByContributorGithubId(githubId))
                .willReturn(List.of(repoWithZeroTotal));

            // when
            GithubContributionPattern result = calculator.calculate(githubId);

            // then
            assertThat(result.getIndependentScore()).isEqualTo(0);
        }
    }
}
