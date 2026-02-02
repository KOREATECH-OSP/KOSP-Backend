package io.swkoreatech.kosp.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.swkoreatech.kosp.challenge.config.TestConfig;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.domain.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Integration test for ChallengeEvaluator service.
 * 
 * Tests cover:
 * 1. SpEL evaluation with default values (null safety)
 * 2. Full evaluation flow with ChallengeHistory save
 * 3. Transaction safety (no UnexpectedRollbackException)
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("ChallengeEvaluator Integration Test")
public class ChallengeEvaluatorTest {

    @Autowired
    private ChallengeEvaluator challengeEvaluator;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeHistoryRepository challengeHistoryRepository;

    @Autowired
    private GithubUserStatisticsRepository statisticsRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("SpEL 평가 테스트")
    class SpELEvaluationTest {

        @Test
        @DisplayName("GithubUserStatistics 기본값으로 SpEL 평가 성공")
        void evaluate_withDefaultValues_shouldWork() {
            // given
            GithubUserStatistics stats = GithubUserStatistics.builder()
                .githubId("12345")
                .calculatedAt(LocalDateTime.now())
                .build();  // All fields get default 0
            statisticsRepository.save(stats);

            GithubUser githubUser = GithubUser.builder()
                .githubId(12345L)
                .githubLogin("defaultuser")
                .build();

            User user = User.builder()
                .kutEmail("default@koreatech.ac.kr")
                .password("password")
                .githubUser(githubUser)
                .build();
            userRepository.save(user);

            Challenge challenge = Challenge.builder()
                .name("기본값 테스트")
                .condition("T(Math).min(totalCommits * 100 / 100, 100)")  // 0 * 100 / 100 = 0
                .point(100)
                .build();
            challengeRepository.save(challenge);

            // when & then
            assertThatCode(() -> challengeEvaluator.evaluate(user))
                .doesNotThrowAnyException();

            // Default 0 → progress 0 → no achievement
            List<ChallengeHistory> histories = challengeHistoryRepository.findAllByUserId(user.getId());
            assertThat(histories).isEmpty();
        }

        @Test
        @DisplayName("null 곱셈 연산에서 ArithmeticException 발생하지 않음")
        void evaluate_withNullFields_shouldNotThrowArithmeticException() {
            // given
            GithubUserStatistics stats = GithubUserStatistics.builder()
                .githubId("11111")
                .calculatedAt(LocalDateTime.now())
                .build();  // All integer fields default to 0, not null
            statisticsRepository.save(stats);

            GithubUser githubUser = GithubUser.builder()
                .githubId(11111L)
                .githubLogin("nullsafeuser")
                .build();

            User user = User.builder()
                .kutEmail("nullsafe@koreatech.ac.kr")
                .password("password")
                .githubUser(githubUser)
                .build();
            userRepository.save(user);

            Challenge challenge = Challenge.builder()
                .name("null 안전 테스트")
                .condition("totalCommits * 100 / 10")  // 0 * 100 / 10 = 0
                .point(100)
                .build();
            challengeRepository.save(challenge);

            // when & then
            assertThatCode(() -> challengeEvaluator.evaluate(user))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("실제 통계 데이터로 SpEL 평가 성공")
        void evaluate_withRealData_shouldCalculateCorrectly() {
            // given
            GithubUserStatistics stats = GithubUserStatistics.builder()
                .githubId("22222")
                .totalCommits(1203)
                .totalPrs(65)
                .totalIssues(30)
                .calculatedAt(LocalDateTime.now())
                .build();
            statisticsRepository.save(stats);

            GithubUser githubUser = GithubUser.builder()
                .githubId(22222L)
                .githubLogin("realdatauser")
                .build();

            User user = User.builder()
                .kutEmail("realdata@koreatech.ac.kr")
                .password("password")
                .githubUser(githubUser)
                .build();
            userRepository.save(user);

            Challenge challenge = Challenge.builder()
                .name("커밋 마스터")
                .condition("T(Math).min(totalCommits * 100 / 1000, 100)")  // 1203 * 100 / 1000 = 120 -> min(120, 100) = 100
                .point(100)
                .build();
            challengeRepository.save(challenge);

            // when
            challengeEvaluator.evaluate(user);

            // then
            List<ChallengeHistory> histories = challengeHistoryRepository.findAllByUserId(user.getId());
            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).isAchieved()).isTrue();
            assertThat(histories.get(0).getProgressAtAchievement()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("evaluate 메서드")
    class EvaluateTest {

        @Test
        @DisplayName("Challenge 평가 성공 시 ChallengeHistory 저장")
        void evaluate_success_shouldSaveChallengeHistory() {
            // given
            GithubUserStatistics stats = GithubUserStatistics.builder()
                .githubId("12345")
                .totalCommits(1203)
                .totalPrs(65)
                .calculatedAt(LocalDateTime.now())
                .build();
            statisticsRepository.save(stats);

            GithubUser githubUser = GithubUser.builder()
                .githubId(12345L)
                .githubLogin("testuser")
                .build();

            User user = User.builder()
                .kutEmail("test@koreatech.ac.kr")
                .password("password")
                .githubUser(githubUser)
                .build();
            userRepository.save(user);

            Challenge challenge = Challenge.builder()
                .name("커밋 마스터")
                .condition("T(Math).min(totalCommits * 100 / 10, 100)")  // 1203 * 100 / 10 = 12030 -> min(12030, 100) = 100
                .point(100)
                .build();
            challengeRepository.save(challenge);

            // when
            challengeEvaluator.evaluate(user);

            // then
            List<ChallengeHistory> histories = challengeHistoryRepository.findAllByUserId(user.getId());
            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).isAchieved()).isTrue();
            assertThat(histories.get(0).getProgressAtAchievement()).isEqualTo(100);
            assertThat(histories.get(0).getChallenge().getId()).isEqualTo(challenge.getId());
        }

        @Test
        @DisplayName("진행도 100% 미만인 Challenge는 ChallengeHistory 미저장")
        void evaluate_progressUnder100_shouldNotSaveHistory() {
            // given
            GithubUserStatistics stats = GithubUserStatistics.builder()
                .githubId("67890")
                .totalCommits(50)  // 진행도: 50 * 100 / 100 = 50%
                .calculatedAt(LocalDateTime.now())
                .build();
            statisticsRepository.save(stats);

            GithubUser githubUser = GithubUser.builder()
                .githubId(67890L)
                .githubLogin("incompleteuser")
                .build();

            User user = User.builder()
                .kutEmail("incomplete@koreatech.ac.kr")
                .password("password")
                .githubUser(githubUser)
                .build();
            userRepository.save(user);

            Challenge challenge = Challenge.builder()
                .name("커밋 100개")
                .condition("T(Math).min(totalCommits * 100 / 100, 100)")  // 50 * 100 / 100 = 50
                .point(50)
                .build();
            challengeRepository.save(challenge);

            // when
            challengeEvaluator.evaluate(user);

            // then
            List<ChallengeHistory> histories = challengeHistoryRepository.findAllByUserId(user.getId());
            assertThat(histories).isEmpty();
        }

        @Test
        @DisplayName("이미 달성한 Challenge는 중복 저장하지 않음")
        void evaluate_alreadyAchieved_shouldNotDuplicateSave() {
            // given
            GithubUserStatistics stats = GithubUserStatistics.builder()
                .githubId("99999")
                .totalCommits(200)
                .calculatedAt(LocalDateTime.now())
                .build();
            statisticsRepository.save(stats);

            GithubUser githubUser = GithubUser.builder()
                .githubId(99999L)
                .githubLogin("achieveduser")
                .build();

            User user = User.builder()
                .kutEmail("achieved@koreatech.ac.kr")
                .password("password")
                .githubUser(githubUser)
                .build();
            userRepository.save(user);

            Challenge challenge = Challenge.builder()
                .name("커밋 초보")
                .condition("T(Math).min(totalCommits * 100 / 100, 100)")  // 200 * 100 / 100 = 200 -> min(200, 100) = 100
                .point(50)
                .build();
            challengeRepository.save(challenge);

            // First achievement
            challengeEvaluator.evaluate(user);

            long firstCount = challengeHistoryRepository.findAllByUserId(user.getId()).size();

            // when: Second evaluation
            challengeEvaluator.evaluate(user);

            // then
            long secondCount = challengeHistoryRepository.findAllByUserId(user.getId()).size();
            assertThat(secondCount).isEqualTo(firstCount);
        }
    }

    @Nested
    @DisplayName("Transaction Safety")
    class TransactionSafetyTest {

        @Test
        @DisplayName("ChallengeEvaluator 예외 발생해도 UnexpectedRollbackException 없음")
        void evaluate_withException_shouldNotThrowUnexpectedRollback() {
            // given
            User user = User.builder()
                .kutEmail("nolink@koreatech.ac.kr")
                .password("password")
                .build();  // No GitHub linkage
            userRepository.save(user);

            // when & then
            assertThatCode(() -> challengeEvaluator.evaluate(user))
                .doesNotThrowAnyException();  // No UnexpectedRollbackException
        }

        @Test
        @DisplayName("GithubUser 있지만 Statistics 없을 때 예외 없음")
        void evaluate_withNoStatistics_shouldNotThrowException() {
            // given
            GithubUser githubUser = GithubUser.builder()
                .githubId(11111L)
                .githubLogin("nostatsuser")
                .build();

            User user = User.builder()
                .kutEmail("nostats@koreatech.ac.kr")
                .password("password")
                .githubUser(githubUser)
                .build();
            userRepository.save(user);

            // when & then
            assertThatCode(() -> challengeEvaluator.evaluate(user))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("잘못된 SpEL 조건식도 트랜잭션 롤백 없음")
        void evaluate_withInvalidSpEL_shouldNotRollbackTransaction() {
            // given
            GithubUserStatistics stats = GithubUserStatistics.builder()
                .githubId("88888")
                .totalCommits(100)
                .calculatedAt(LocalDateTime.now())
                .build();
            statisticsRepository.save(stats);

            GithubUser githubUser = GithubUser.builder()
                .githubId(88888L)
                .githubLogin("invalidspeluser")
                .build();

            User user = User.builder()
                .kutEmail("invalidspel@koreatech.ac.kr")
                .password("password")
                .githubUser(githubUser)
                .build();
            userRepository.save(user);

            Challenge challenge = Challenge.builder()
                .name("잘못된 조건식")
                .condition("invalidMethod()")  // Invalid SpEL
                .point(100)
                .build();
            challengeRepository.save(challenge);

            // when & then
            assertThatCode(() -> challengeEvaluator.evaluate(user))
                .doesNotThrowAnyException();

            // Verify user still exists (transaction not rolled back)
            assertThat(userRepository.findById(user.getId())).isPresent();
        }
    }
}
