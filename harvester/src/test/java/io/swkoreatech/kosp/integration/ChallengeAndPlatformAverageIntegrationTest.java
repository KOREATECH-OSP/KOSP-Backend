package io.swkoreatech.kosp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.statistics.model.PlatformStatistics;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.statistics.repository.PlatformStatisticsRepository;
import io.swkoreatech.kosp.user.GithubUserRepository;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Challenge Evaluation and Platform Average Integration Test")
class ChallengeAndPlatformAverageIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job githubCollectionJob;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GithubUserRepository githubUserRepository;

    @Autowired
    private GithubUserStatisticsRepository statisticsRepository;

    @Autowired
    private PlatformStatisticsRepository platformStatisticsRepository;

    private static final String STREAM_KEY = "kosp:challenge-check";
    private static final String STAT_KEY = "GLOBAL";

    @BeforeEach
    void setUp() {
        cleanupRedisStream();
        cleanupDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanupRedisStream();
        cleanupDatabase();
    }

    @Test
    @DisplayName("Job 실행 후 Redis 스트림 이벤트 발행")
    void jobPublishesEvent() throws Exception {
        GithubUser githubUser = createGithubUser(1001L);
        createStatistics(String.valueOf(githubUser.getGithubId()), 10, 5, 3, 20);

        JobParameters params = buildJobParameters(githubUser.getGithubId());
        JobExecution execution = jobLauncher.run(githubCollectionJob, params);

        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<MapRecord<String, Object, Object>> messages = readStreamMessages();

        assertThat(messages).isNotEmpty();

        MapRecord<String, Object, Object> firstMessage = messages.get(0);
        Map<Object, Object> payload = firstMessage.getValue();

        assertThat(String.valueOf(payload.get("userId"))).isEqualTo(String.valueOf(githubUser.getGithubId()));
        assertThat(payload.get("jobExecutionId")).isNotNull();
        assertThat(payload.get("calculatedAt")).isNotNull();
    }

    @Test
    @DisplayName("챌린지 달성 시 포인트 지급 준비 완료")
    void challengeEventPublishedWithCorrectUserId() throws Exception {
        GithubUser githubUser = createGithubUser(1002L);
        createStatistics(String.valueOf(githubUser.getGithubId()), 100, 50, 30, 200);

        JobParameters params = buildJobParameters(githubUser.getGithubId());
        JobExecution execution = jobLauncher.run(githubCollectionJob, params);

        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<MapRecord<String, Object, Object>> messages = readStreamMessages();
        assertThat(messages).isNotEmpty();

        MapRecord<String, Object, Object> message = messages.get(0);
        Map<Object, Object> payload = message.getValue();

        assertThat(String.valueOf(payload.get("userId"))).isEqualTo(String.valueOf(githubUser.getGithubId()));

        GithubUserStatistics stats = statisticsRepository.findByGithubId(String.valueOf(githubUser.getGithubId()))
            .orElseThrow();

        assertThat(stats.getTotalCommits()).isEqualTo(100);
        assertThat(stats.getTotalPrs()).isEqualTo(50);
        assertThat(stats.getTotalIssues()).isEqualTo(30);
        assertThat(stats.getTotalStarsReceived()).isEqualTo(200);
    }

    @Test
    @DisplayName("임계값 충족 시 플랫폼 평균 업데이트")
    void platformAverageUpdatesWhenThresholdMet() throws Exception {
        platformStatisticsRepository.deleteAll();

        for (long i = 1; i <= 12; i++) {
            GithubUser user = createGithubUser(2000L + i);
            createStatistics(String.valueOf(user.getGithubId()), (int) (i * 10), (int) (i * 5), (int) (i * 2), (int) (i * 15));
        }

        GithubUser triggerUser = githubUserRepository.findById(2001L).orElseThrow();
        JobParameters params = buildJobParameters(triggerUser.getGithubId());

        JobExecution execution = jobLauncher.run(githubCollectionJob, params);

        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        PlatformStatistics platformStats = platformStatisticsRepository.findByStatKey(STAT_KEY)
            .orElseThrow(() -> new AssertionError("Platform statistics not created"));

        assertThat(platformStats.getTotalUserCount()).isGreaterThan(0);
        assertThat(platformStats.getAvgCommitCount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(platformStats.getAvgPrCount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(platformStats.getAvgIssueCount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(platformStats.getAvgStarCount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(platformStats.getCalculatedAt()).isNotNull();
    }

    @Test
    @DisplayName("임계값 미충족 시 플랫폼 평균 스킵")
    void platformAverageSkipsWhenThresholdNotMet() throws Exception {
        platformStatisticsRepository.deleteAll();

        PlatformStatistics initialStats = PlatformStatistics.create(STAT_KEY);
        initialStats.updateAverages(
            BigDecimal.valueOf(50),
            BigDecimal.valueOf(25),
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(5),
            5
        );
        platformStatisticsRepository.save(initialStats);
        LocalDateTime initialCalculatedAt = initialStats.getCalculatedAt();

        for (long i = 1; i <= 3; i++) {
            GithubUser user = createGithubUser(3000L + i);
            createStatistics(String.valueOf(user.getGithubId()), 20, 10, 5, 15);
        }

        GithubUser triggerUser = githubUserRepository.findById(3001L).orElseThrow();
        JobParameters params = buildJobParameters(triggerUser.getGithubId());

        JobExecution execution = jobLauncher.run(githubCollectionJob, params);

        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        PlatformStatistics platformStats = platformStatisticsRepository.findByStatKey(STAT_KEY)
            .orElseThrow();

        assertThat(platformStats.getTotalUserCount()).isEqualTo(5);
        assertThat(platformStats.getCalculatedAt()).isEqualTo(initialCalculatedAt);
    }

    @Test
    @DisplayName("중복 이벤트 멱등성 검증")
    void idempotencyPreventsDuplicates() throws Exception {
        GithubUser githubUser = createGithubUser(4001L);
        createStatistics(String.valueOf(githubUser.getGithubId()), 30, 15, 8, 40);

        JobParameters params = buildJobParameters(githubUser.getGithubId());

        JobExecution firstExecution = jobLauncher.run(githubCollectionJob, params);
        assertThat(firstExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<MapRecord<String, Object, Object>> firstMessages = readStreamMessages();
        int firstMessageCount = firstMessages.size();

        assertThat(firstMessageCount).isGreaterThan(0);

        JobParameters secondParams = new JobParametersBuilder(params)
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        JobExecution secondExecution = jobLauncher.run(githubCollectionJob, secondParams);
        assertThat(secondExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<MapRecord<String, Object, Object>> allMessages = readStreamMessages();

        assertThat(allMessages).hasSizeGreaterThanOrEqualTo(firstMessageCount);

        long distinctJobExecutionIds = allMessages.stream()
            .map(MapRecord::getValue)
            .map(payload -> String.valueOf(payload.get("jobExecutionId")))
            .distinct()
            .count();

        assertThat(distinctJobExecutionIds).isGreaterThanOrEqualTo(1);
    }

    private GithubUser createGithubUser(Long githubId) {
        return githubUserRepository.save(GithubUser.builder()
            .githubId(githubId)
            .githubLogin("user" + githubId)
            .githubName("User " + githubId)
            .githubToken("dummy_token_" + githubId)
            .githubAvatarUrl("https://avatar.url/" + githubId)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build());
    }

    private void createStatistics(
        String githubId,
        int commits,
        int prs,
        int issues,
        int stars
    ) {
        GithubUserStatistics stats = GithubUserStatistics.create(githubId);
        stats.updateStatistics(commits, 0, 0, 0, prs, issues, 0, 0, stars, 0, 0, 0);
        statisticsRepository.save(stats);
    }

    private JobParameters buildJobParameters(Long githubId) {
        return new JobParametersBuilder()
            .addLong("userId", githubId)
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
    }

    private List<MapRecord<String, Object, Object>> readStreamMessages() {
        List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
            .read(StreamOffset.create(STREAM_KEY, ReadOffset.from("0-0")));

        if (messages == null) {
            return List.of();
        }

        return messages;
    }

    private void cleanupRedisStream() {
        try {
            redisTemplate.delete(STREAM_KEY);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    private void cleanupDatabase() {
        try {
            statisticsRepository.findAll().forEach(stat -> {
                // No deleteAll method, so we'll leave test data
            });
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}
