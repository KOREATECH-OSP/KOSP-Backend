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

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonthlyStatisticsCalculator 단위 테스트")
class MonthlyStatisticsCalculatorTest {

    @InjectMocks
    private MonthlyStatisticsCalculator calculator;

    @Mock
    private GithubCommitDetailRawRepository commitDetailRawRepository;

    private GithubCommitDetailRaw createCommit(String githubId, String date, int additions, int deletions) {
        return GithubCommitDetailRaw.builder()
            .sha("sha-" + date)
            .repoOwner("owner")
            .repoName("repo")
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
        @DisplayName("커밋이 없으면 빈 리스트를 반환한다")
        void returnsEmptyList_whenNoCommits() {
            // given
            String githubId = "testuser";
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(List.of());

            // when
            List<GithubMonthlyStatistics> result = calculator.calculate(githubId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("월별로 커밋을 그룹화하여 통계를 계산한다")
        void calculatesMonthlyStatistics() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50),
                createCommit(githubId, "2024-01-20T10:00:00Z", 200, 100),
                createCommit(githubId, "2024-02-10T10:00:00Z", 50, 25)
            );
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);

            // when
            List<GithubMonthlyStatistics> result = calculator.calculate(githubId);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("같은 월의 커밋 수와 라인 수를 합산한다")
        void sumsCommitsAndLinesForSameMonth() {
            // given
            String githubId = "testuser";
            List<GithubCommitDetailRaw> commits = List.of(
                createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50),
                createCommit(githubId, "2024-01-20T10:00:00Z", 200, 100)
            );
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(commits);

            // when
            List<GithubMonthlyStatistics> result = calculator.calculate(githubId);

            // then
            assertThat(result).hasSize(1);
            GithubMonthlyStatistics januaryStats = result.get(0);
            assertThat(januaryStats.getCommitsCount()).isEqualTo(2);
            assertThat(januaryStats.getAdditionsCount()).isEqualTo(300);
            assertThat(januaryStats.getDeletionsCount()).isEqualTo(150);
            assertThat(januaryStats.getLinesCount()).isEqualTo(450);
        }

        @Test
        @DisplayName("여러 저장소에 기여한 경우 저장소 수를 계산한다")
        void countsDistinctRepositories() {
            // given
            String githubId = "testuser";
            GithubCommitDetailRaw commit1 = GithubCommitDetailRaw.builder()
                .sha("sha1")
                .repoOwner("owner1")
                .repoName("repo1")
                .author(Map.of("login", githubId, "date", "2024-01-15T10:00:00Z"))
                .stats(Map.of("additions", 10, "deletions", 5))
                .build();
            GithubCommitDetailRaw commit2 = GithubCommitDetailRaw.builder()
                .sha("sha2")
                .repoOwner("owner2")
                .repoName("repo2")
                .author(Map.of("login", githubId, "date", "2024-01-20T10:00:00Z"))
                .stats(Map.of("additions", 20, "deletions", 10))
                .build();
            given(commitDetailRawRepository.findByAuthorLogin(githubId)).willReturn(List.of(commit1, commit2));

            // when
            List<GithubMonthlyStatistics> result = calculator.calculate(githubId);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("날짜가 없는 커밋은 필터링된다")
        void filtersCommitsWithoutDate() {
            // given
            String githubId = "testuser";
            GithubCommitDetailRaw commitWithDate = createCommit(githubId, "2024-01-15T10:00:00Z", 100, 50);
            GithubCommitDetailRaw commitWithoutDate = GithubCommitDetailRaw.builder()
                .sha("sha-no-date")
                .repoOwner("owner")
                .repoName("repo")
                .author(Map.of("login", githubId))
                .stats(Map.of("additions", 200, "deletions", 100))
                .build();
            given(commitDetailRawRepository.findByAuthorLogin(githubId))
                .willReturn(List.of(commitWithDate, commitWithoutDate));

            // when
            List<GithubMonthlyStatistics> result = calculator.calculate(githubId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCommitsCount()).isEqualTo(1);
        }
    }
}
