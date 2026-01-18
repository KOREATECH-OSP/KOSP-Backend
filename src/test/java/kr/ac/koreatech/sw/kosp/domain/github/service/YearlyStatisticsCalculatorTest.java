package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubYearlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubYearlyStatisticsRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("YearlyStatisticsCalculator 단위 테스트")
class YearlyStatisticsCalculatorTest {

    @InjectMocks
    private YearlyStatisticsCalculator calculator;

    @Mock
    private GithubCommitDetailRawRepository commitDetailRawRepository;

    @Mock
    private GithubYearlyStatisticsRepository yearlyStatisticsRepository;

    @Mock
    private GithubPRIssueCountService prIssueCountService;

    private GithubCommitDetailRaw createCommit(String githubId, String date, int additions, int deletions, String repoOwner, String repoName) {
        return GithubCommitDetailRaw.builder()
            .sha("sha-" + date)
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

    @Nested
    @DisplayName("calculate 메서드")
    class CalculateTest {

        @Test
        @DisplayName("해당 연도에 커밋이 없으면 빈 통계를 반환한다")
        void returnsEmptyStats_whenNoCommitsInYear() {
            // given
            String githubId = "testuser";
            int year = 2024;
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(List.of());

            // when
            GithubYearlyStatistics result = calculator.calculate(githubId, year);

            // then
            assertThat(result.getGithubId()).isEqualTo(githubId);
            assertThat(result.getYear()).isEqualTo(year);
        }

        @Test
        @DisplayName("해당 연도의 커밋 통계를 계산한다")
        void calculatesYearStatistics() {
            // given
            String githubId = "testuser";
            int year = 2024;
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50, "owner", "repo1"),
                createCommit(githubId, "2024-06-20T10:00:00Z", 200, 100, "owner", "repo2")
            );
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(prIssueCountService.countUserPRsByYear(githubId, year)).willReturn(5);
            given(prIssueCountService.countUserIssuesByYear(githubId, year)).willReturn(3);

            // when
            GithubYearlyStatistics result = calculator.calculate(githubId, year);

            // then
            assertThat(result.getCommits()).isEqualTo(2);
            assertThat(result.getAdditions()).isEqualTo(300);
            assertThat(result.getDeletions()).isEqualTo(150);
            assertThat(result.getPrs()).isEqualTo(5);
            assertThat(result.getIssues()).isEqualTo(3);
        }

        @Test
        @DisplayName("다른 연도의 커밋은 필터링된다")
        void filtersCommitsFromOtherYears() {
            // given
            String githubId = "testuser";
            int year = 2024;
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50, "owner", "repo"),
                createCommit(githubId, "2023-12-20T10:00:00Z", 999, 999, "owner", "repo")
            );
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(prIssueCountService.countUserPRsByYear(githubId, year)).willReturn(0);
            given(prIssueCountService.countUserIssuesByYear(githubId, year)).willReturn(0);

            // when
            GithubYearlyStatistics result = calculator.calculate(githubId, year);

            // then
            assertThat(result.getCommits()).isEqualTo(1);
            assertThat(result.getAdditions()).isEqualTo(100);
        }

        @Test
        @DisplayName("가장 많이 기여한 저장소를 찾는다")
        void findsBestRepository() {
            // given
            String githubId = "testuser";
            int year = 2024;
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-10T10:00:00Z", 10, 5, "owner1", "repo1"),
                createCommit(githubId, "2024-01-15T10:00:00Z", 10, 5, "owner1", "repo1"),
                createCommit(githubId, "2024-01-20T10:00:00Z", 10, 5, "owner1", "repo1"),
                createCommit(githubId, "2024-02-10T10:00:00Z", 20, 10, "owner2", "repo2")
            );
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(prIssueCountService.countUserPRsByYear(githubId, year)).willReturn(0);
            given(prIssueCountService.countUserIssuesByYear(githubId, year)).willReturn(0);

            // when
            GithubYearlyStatistics result = calculator.calculate(githubId, year);

            // then
            assertThat(result.getBestRepoOwner()).isEqualTo("owner1");
            assertThat(result.getBestRepoName()).isEqualTo("repo1");
            assertThat(result.getBestRepoCommits()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("calculateAll 메서드")
    class CalculateAllTest {

        @Test
        @DisplayName("커밋이 없으면 빈 리스트를 반환한다")
        void returnsEmptyList_whenNoCommits() {
            // given
            String githubId = "testuser";
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(List.of());

            // when
            List<GithubYearlyStatistics> result = calculator.calculateAll(githubId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("모든 연도의 통계를 계산한다")
        void calculatesAllYears() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2023-06-15T10:00:00Z", 50, 25, "owner", "repo"),
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50, "owner", "repo"),
                createCommit(githubId, "2024-06-15T10:00:00Z", 150, 75, "owner", "repo")
            );
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);
            given(prIssueCountService.countUserPRsByYear(githubId, 2023)).willReturn(0);
            given(prIssueCountService.countUserIssuesByYear(githubId, 2023)).willReturn(0);
            given(prIssueCountService.countUserPRsByYear(githubId, 2024)).willReturn(0);
            given(prIssueCountService.countUserIssuesByYear(githubId, 2024)).willReturn(0);

            // when
            List<GithubYearlyStatistics> result = calculator.calculateAll(githubId);

            // then
            assertThat(result).hasSize(2);
        }
    }
}
