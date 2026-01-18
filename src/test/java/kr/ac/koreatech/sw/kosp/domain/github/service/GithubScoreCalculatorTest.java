package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Collections;
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

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubIssueRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubPRRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
@DisplayName("GithubScoreCalculator 단위 테스트")
class GithubScoreCalculatorTest {

    @InjectMocks
    private GithubScoreCalculator scoreCalculator;

    @Mock
    private GithubUserStatisticsRepository userStatisticsRepository;

    @Mock
    private GithubRepositoryStatisticsRepository repoStatisticsRepository;

    @Mock
    private GithubPRRawRepository prRawRepository;

    @Mock
    private GithubIssueRawRepository issueRawRepository;

    private GithubUserStatistics createUserStatistics(String githubId, int contributedReposCount) {
        GithubUserStatistics stats = GithubUserStatistics.create(githubId);
        ReflectionTestUtils.setField(stats, "id", 1L);
        ReflectionTestUtils.setField(stats, "contributedReposCount", contributedReposCount);
        return stats;
    }

    private GithubRepositoryStatistics createRepoStatistics(
        String githubId, 
        int commits, 
        int prs, 
        Boolean isOwned, 
        Integer stars
    ) {
        GithubRepositoryStatistics stats = GithubRepositoryStatistics.create("owner", "repo", githubId);
        ReflectionTestUtils.setField(stats, "id", 1L);
        ReflectionTestUtils.setField(stats, "userCommitsCount", commits);
        ReflectionTestUtils.setField(stats, "userPrsCount", prs);
        ReflectionTestUtils.setField(stats, "isOwned", isOwned);
        ReflectionTestUtils.setField(stats, "stargazersCount", stars);
        return stats;
    }

    @Nested
    @DisplayName("calculate 메서드")
    class CalculateTest {

        @Test
        @DisplayName("사용자 통계가 없으면 예외가 발생한다")
        void throwsException_whenUserStatisticsNotFound() {
            // given
            String githubId = "testuser";
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreCalculator.calculate(githubId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User statistics not found");
        }

        @Test
        @DisplayName("활동이 전혀 없으면 0점을 반환한다")
        void returnsZero_whenNoActivity() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 0);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(Collections.emptyList());
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(0.00).setScale(2));
            verify(userStatisticsRepository).save(userStats);
        }

        @Test
        @DisplayName("커밋 5회 이상이면 활동 수준 1점을 얻는다")
        void returnsActivityLevel1_whenCommits5OrMore() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 1);
            GithubRepositoryStatistics repoStats = createRepoStatistics(githubId, 5, 0, false, 0);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of(repoStats));
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(1.00).setScale(2));
        }

        @Test
        @DisplayName("커밋 30회 이상, PR 5회 이상이면 활동 수준 2점을 얻는다")
        void returnsActivityLevel2_whenCommits30AndPrs5() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 1);
            GithubRepositoryStatistics repoStats = createRepoStatistics(githubId, 30, 5, false, 0);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of(repoStats));
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(2.00).setScale(2));
        }

        @Test
        @DisplayName("커밋 100회 이상, PR 20회 이상이면 활동 수준 3점을 얻는다")
        void returnsActivityLevel3_whenCommits100AndPrs20() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 1);
            GithubRepositoryStatistics repoStats = createRepoStatistics(githubId, 100, 20, false, 0);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of(repoStats));
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(3.00).setScale(2));
        }

        @Test
        @DisplayName("기여 저장소 10개 이상이면 다양성 1점을 얻는다")
        void returnsDiversity1_whenContributedRepos10OrMore() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 10);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(Collections.emptyList());
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(1.00).setScale(2));
        }

        @Test
        @DisplayName("기여 저장소 5-9개면 다양성 0.7점을 얻는다")
        void returnsDiversity07_whenContributedRepos5to9() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 5);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(Collections.emptyList());
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(0.70).setScale(2));
        }

        @Test
        @DisplayName("기여 저장소 2-4개면 다양성 0.4점을 얻는다")
        void returnsDiversity04_whenContributedRepos2to4() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 3);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(Collections.emptyList());
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(0.40).setScale(2));
        }

        @Test
        @DisplayName("본인 저장소에 스타 100개 이상이면 영향성 2점 보너스를 얻는다")
        void returnsImpact2_whenOwnedRepoHas100Stars() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 1);
            GithubRepositoryStatistics repoStats = createRepoStatistics(githubId, 0, 0, true, 100);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of(repoStats));
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());
            given(issueRawRepository.findByRepoOwnerAndRepoName("owner", "repo")).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(2.00).setScale(2));
        }

        @Test
        @DisplayName("복합 점수: 활동 3점 + 다양성 1점 + 영향성 2점 = 6점")
        void returnsCompositeScore() {
            // given
            String githubId = "testuser";
            GithubUserStatistics userStats = createUserStatistics(githubId, 10);
            GithubRepositoryStatistics repoStats = createRepoStatistics(githubId, 100, 20, true, 100);
            
            given(userStatisticsRepository.findByGithubId(githubId)).willReturn(Optional.of(userStats));
            given(repoStatisticsRepository.findByContributorGithubId(githubId)).willReturn(List.of(repoStats));
            given(prRawRepository.findByAuthorLogin(githubId)).willReturn(Flux.empty());
            given(issueRawRepository.findByRepoOwnerAndRepoName("owner", "repo")).willReturn(Flux.empty());

            // when
            BigDecimal score = scoreCalculator.calculate(githubId);

            // then
            assertThat(score).isEqualTo(BigDecimal.valueOf(6.00).setScale(2));
        }
    }
}
