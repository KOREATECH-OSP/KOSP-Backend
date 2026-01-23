package io.swkoreatech.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.domain.github.model.GithubContributionPattern;
import io.swkoreatech.kosp.domain.github.model.GithubGlobalStatistics;
import io.swkoreatech.kosp.domain.github.model.GithubMonthlyStatistics;
import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;
import io.swkoreatech.kosp.domain.github.model.GithubYearlyStatistics;
import io.swkoreatech.kosp.domain.github.repository.GithubContributionPatternRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubGlobalStatisticsRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubMonthlyStatisticsRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubYearlyStatisticsRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubTimelineDataRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("GithubStatisticsService 단위 테스트")
class GithubStatisticsServiceTest {

    @InjectMocks
    private GithubStatisticsService statisticsService;

    @Mock
    private UserStatisticsCalculator userStatisticsCalculator;

    @Mock
    private MonthlyStatisticsCalculator monthlyStatisticsCalculator;

    @Mock
    private RepositoryStatisticsCalculator repositoryStatisticsCalculator;

    @Mock
    private ContributionPatternCalculator contributionPatternCalculator;

    @Mock
    private YearlyStatisticsCalculator yearlyStatisticsCalculator;

    @Mock
    private GithubScoreCalculator scoreCalculator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GithubUserStatisticsRepository userStatisticsRepository;

    @Mock
    private GithubMonthlyStatisticsRepository monthlyStatisticsRepository;

    @Mock
    private GithubRepositoryStatisticsRepository repositoryStatisticsRepository;

    @Mock
    private GithubContributionPatternRepository contributionPatternRepository;

    @Mock
    private GithubYearlyStatisticsRepository yearlyStatisticsRepository;

    @Mock
    private GithubGlobalStatisticsRepository globalStatisticsRepository;

    @Mock
    private GithubTimelineDataRepository timelineDataRepository;

    @Mock
    private ObjectMapper objectMapper;

    private User createUser(Long id, String name, String githubLogin) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);

        GithubUser githubUser = GithubUser.builder()
            .githubId(id)
            .githubLogin(githubLogin)
            .build();
        ReflectionTestUtils.setField(user, "githubUser", githubUser);
        return user;
    }

    private GithubUserStatistics createUserStatistics(String githubId) {
        GithubUserStatistics stats = GithubUserStatistics.create(githubId);
        ReflectionTestUtils.setField(stats, "id", 1L);
        ReflectionTestUtils.setField(stats, "totalCommits", 100);
        ReflectionTestUtils.setField(stats, "totalPrs", 20);
        ReflectionTestUtils.setField(stats, "totalIssues", 10);
        ReflectionTestUtils.setField(stats, "contributedReposCount", 5);
        ReflectionTestUtils.setField(stats, "totalStarsReceived", 50);
        ReflectionTestUtils.setField(stats, "totalScore", BigDecimal.valueOf(5.5));
        ReflectionTestUtils.setField(stats, "calculatedAt", LocalDateTime.now());
        return stats;
    }

    private GithubRepositoryStatistics createRepoStatistics(String githubId, String repoName) {
        GithubRepositoryStatistics stats = GithubRepositoryStatistics.create("owner", repoName, githubId);
        ReflectionTestUtils.setField(stats, "id", 1L);
        ReflectionTestUtils.setField(stats, "userCommitsCount", 50);
        ReflectionTestUtils.setField(stats, "userPrsCount", 10);
        ReflectionTestUtils.setField(stats, "lastCommitDate", LocalDateTime.now());
        ReflectionTestUtils.setField(stats, "stargazersCount", 100);
        ReflectionTestUtils.setField(stats, "primaryLanguage", "Java");
        return stats;
    }

    @Nested
    @DisplayName("calculateAndSaveUserStatistics 메서드")
    class CalculateAndSaveUserStatisticsTest {

        @Test
        @DisplayName("새 사용자 통계를 계산하고 저장한다")
        void calculatesAndSavesNewStatistics() {
            // given
            String githubId = "testuser";
            GithubUserStatistics calculated = createUserStatistics(githubId);
            
            given(userStatisticsCalculator.calculate(githubId)).willReturn(calculated);
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.empty());
            given(userStatisticsRepository.save(any())).willReturn(calculated);

            // when
            GithubUserStatistics result = statisticsService.calculateAndSaveUserStatistics(githubId);

            // then
            assertThat(result.getGithubId()).isEqualTo(githubId);
            verify(userStatisticsRepository).save(calculated);
        }

        @Test
        @DisplayName("기존 사용자 통계를 업데이트한다")
        void updatesExistingStatistics() {
            // given
            String githubId = "testuser";
            GithubUserStatistics existing = createUserStatistics(githubId);
            GithubUserStatistics calculated = createUserStatistics(githubId);
            
            given(userStatisticsCalculator.calculate(githubId)).willReturn(calculated);
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(existing));
            given(userStatisticsRepository.save(any())).willReturn(existing);

            // when
            GithubUserStatistics result = statisticsService.calculateAndSaveUserStatistics(githubId);

            // then
            assertThat(result).isNotNull();
            verify(userStatisticsRepository).save(existing);
        }
    }

    @Nested
    @DisplayName("calculateAndSaveMonthlyStatistics 메서드")
    class CalculateAndSaveMonthlyStatisticsTest {

        @Test
        @DisplayName("월별 통계를 계산하고 저장한다")
        void calculatesAndSavesMonthlyStatistics() {
            // given
            String githubId = "testuser";
            GithubMonthlyStatistics monthly = GithubMonthlyStatistics.create(githubId, 2024, 1);
            
            given(monthlyStatisticsCalculator.calculate(githubId)).willReturn(List.of(monthly));
            given(monthlyStatisticsRepository.findByGithubId(githubId)).willReturn(List.of());
            given(monthlyStatisticsRepository.saveAll(any())).willReturn(List.of(monthly));

            // when
            List<GithubMonthlyStatistics> result = statisticsService.calculateAndSaveMonthlyStatistics(githubId);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("calculateAndSaveRepositoryStatistics 메서드")
    class CalculateAndSaveRepositoryStatisticsTest {

        @Test
        @DisplayName("저장소별 통계를 계산하고 저장한다")
        void calculatesAndSavesRepoStatistics() {
            // given
            String githubId = "testuser";
            GithubRepositoryStatistics repo = createRepoStatistics(githubId, "test-repo");
            
            given(repositoryStatisticsCalculator.calculate(githubId)).willReturn(List.of(repo));

            // when
            List<GithubRepositoryStatistics> result = statisticsService.calculateAndSaveRepositoryStatistics(githubId);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("calculateAndUpdateScore 메서드")
    class CalculateAndUpdateScoreTest {

        @Test
        @DisplayName("사용자 점수를 계산하고 업데이트한다")
        void calculatesAndUpdatesScore() {
            // given
            String githubId = "testuser";
            GithubUserStatistics stats = createUserStatistics(githubId);
            
            given(scoreCalculator.calculate(githubId)).willReturn(BigDecimal.valueOf(7.5));
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(stats));

            // when
            statisticsService.calculateAndUpdateScore(githubId);

            // then
            verify(userStatisticsRepository).save(stats);
            assertThat(stats.getTotalScore()).isEqualTo(BigDecimal.valueOf(7.5));
        }

        @Test
        @DisplayName("사용자 통계가 없으면 예외가 발생한다")
        void throwsException_whenStatsNotFound() {
            // given
            String githubId = "testuser";
            given(scoreCalculator.calculate(githubId)).willReturn(BigDecimal.valueOf(7.5));
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> statisticsService.calculateAndUpdateScore(githubId))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getSummary 메서드")
    class GetSummaryTest {

        @Test
        @DisplayName("사용자 GitHub 요약을 조회한다")
        void returnsSummary() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubUserStatistics stats = createUserStatistics("testlogin");
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userStatisticsRepository.findByGithubId("testlogin")).willReturn(Optional.of(stats));

            // when
            var result = statisticsService.getSummary(1L);

            // then
            assertThat(result.githubId()).isEqualTo("testlogin");
            assertThat(result.totalCommits()).isEqualTo(100);
        }

        @Test
        @DisplayName("사용자가 없으면 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> statisticsService.getSummary(999L))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getRecentContributions 메서드")
    class GetRecentContributionsTest {

        @Test
        @DisplayName("최근 기여 저장소를 조회한다")
        void returnsRecentContributions() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubRepositoryStatistics repo = createRepoStatistics("testlogin", "test-repo");
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(repositoryStatisticsRepository.findTopNByContributorGithubIdOrderByLastCommitDateDesc("testlogin", 5))
                .willReturn(List.of(repo));

            // when
            var result = statisticsService.getRecentContributions(1L, 5);

            // then
            assertThat(result.repositories()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getContributionPattern 메서드")
    class GetContributionPatternTest {

        @Test
        @DisplayName("기존 패턴이 있으면 반환한다")
        void returnsExistingPattern() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubContributionPattern pattern = GithubContributionPattern.create("testlogin");
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(contributionPatternRepository.findByGithubId("testlogin")).willReturn(Optional.of(pattern));

            // when
            var result = statisticsService.getContributionPattern(1L);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("패턴이 없으면 계산 후 저장한다")
        void calculatesAndSavesPattern() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubContributionPattern pattern = GithubContributionPattern.create("testlogin");
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(contributionPatternRepository.findByGithubId("testlogin")).willReturn(Optional.empty());
            given(contributionPatternCalculator.calculate("testlogin")).willReturn(pattern);
            given(contributionPatternRepository.save(pattern)).willReturn(pattern);

            // when
            var result = statisticsService.getContributionPattern(1L);

            // then
            assertThat(result).isNotNull();
            verify(contributionPatternRepository).save(pattern);
        }
    }

    @Nested
    @DisplayName("getYearlyAnalysis 메서드")
    class GetYearlyAnalysisTest {

        @Test
        @DisplayName("기존 연도별 통계가 있으면 반환한다")
        void returnsExistingYearlyStats() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubYearlyStatistics yearly = GithubYearlyStatistics.create("testlogin", 2024);
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(yearlyStatisticsRepository.findByGithubIdAndYear("testlogin", 2024)).willReturn(Optional.of(yearly));

            // when
            var result = statisticsService.getYearlyAnalysis(1L, 2024);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("통계가 없으면 계산 후 저장한다")
        void calculatesAndSavesYearlyStats() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubYearlyStatistics yearly = GithubYearlyStatistics.create("testlogin", 2024);
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(yearlyStatisticsRepository.findByGithubIdAndYear("testlogin", 2024)).willReturn(Optional.empty());
            given(yearlyStatisticsCalculator.calculate("testlogin", 2024)).willReturn(yearly);
            given(yearlyStatisticsRepository.save(yearly)).willReturn(yearly);

            // when
            var result = statisticsService.getYearlyAnalysis(1L, 2024);

            // then
            assertThat(result).isNotNull();
            verify(yearlyStatisticsRepository).save(yearly);
        }
    }

    @Nested
    @DisplayName("getRepositoryStats 메서드")
    class GetRepositoryStatsTest {

        @Test
        @DisplayName("저장소 통계를 커밋 기준으로 정렬하여 조회한다")
        void returnsRepoStatsSortedByCommits() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubRepositoryStatistics repo1 = createRepoStatistics("testlogin", "repo1");
            GithubRepositoryStatistics repo2 = createRepoStatistics("testlogin", "repo2");
            ReflectionTestUtils.setField(repo1, "userCommitsCount", 100);
            ReflectionTestUtils.setField(repo2, "userCommitsCount", 50);
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(repositoryStatisticsRepository.findByContributorGithubId("testlogin"))
                .willReturn(List.of(repo1, repo2));

            // when
            var result = statisticsService.getRepositoryStats(1L, 10, "commits");

            // then
            assertThat(result.repositories()).hasSize(2);
        }

        @Test
        @DisplayName("저장소 통계를 스타 기준으로 정렬하여 조회한다")
        void returnsRepoStatsSortedByStars() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubRepositoryStatistics repo = createRepoStatistics("testlogin", "repo");
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(repositoryStatisticsRepository.findByContributorGithubId("testlogin"))
                .willReturn(List.of(repo));

            // when
            var result = statisticsService.getRepositoryStats(1L, 10, "stars");

            // then
            assertThat(result.repositories()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getGlobalStatistics 메서드")
    class GetGlobalStatisticsTest {

        @Test
        @DisplayName("전체 통계가 있으면 반환한다")
        void returnsGlobalStats() {
            // given
            GithubGlobalStatistics global = GithubGlobalStatistics.create(50.0, 10.0, 5.0, 3.0, 100);
            
            given(globalStatisticsRepository.findTopByOrderByCalculatedAtDesc()).willReturn(Optional.of(global));

            // when
            var result = statisticsService.getGlobalStatistics();

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("전체 통계가 없으면 빈 응답을 반환한다")
        void returnsEmptyWhenNoGlobalStats() {
            // given
            given(globalStatisticsRepository.findTopByOrderByCalculatedAtDesc()).willReturn(Optional.empty());

            // when
            var result = statisticsService.getGlobalStatistics();

            // then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getComparisonDetails 메서드")
    class GetComparisonDetailsTest {

        @Test
        @DisplayName("사용자와 전체 통계를 비교하여 반환한다")
        void returnsComparisonDetails() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubUserStatistics stats = createUserStatistics("testlogin");
            GithubGlobalStatistics global = GithubGlobalStatistics.create(50.0, 10.0, 5.0, 3.0, 100);
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userStatisticsRepository.findByGithubId("testlogin")).willReturn(Optional.of(stats));
            given(globalStatisticsRepository.findTopByOrderByCalculatedAtDesc()).willReturn(Optional.of(global));

            // when
            var result = statisticsService.getComparisonDetails(1L);

            // then
            assertThat(result.userCommitCount()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("getScoreDetails 메서드")
    class GetScoreDetailsTest {

        @Test
        @DisplayName("사용자 점수 상세를 반환한다")
        void returnsScoreDetails() {
            // given
            User user = createUser(1L, "testuser", "testlogin");
            GithubUserStatistics stats = createUserStatistics("testlogin");
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userStatisticsRepository.findByGithubId("testlogin")).willReturn(Optional.of(stats));

            // when
            var result = statisticsService.getScoreDetails(1L);

            // then
            assertThat(result).isNotNull();
        }
    }
}
