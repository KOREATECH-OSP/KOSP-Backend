package io.swkoreatech.kosp.job;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.collection.document.DailyCollectionStatDocument;
import io.swkoreatech.kosp.collection.repository.CollectionMetadataRepository;
import io.swkoreatech.kosp.collection.repository.DailyCollectionStatRepository;
import io.swkoreatech.kosp.collection.step.StepContextKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 배치 Job 완료 후 수집 결과를 기록하고 이상 여부를 감지하는 리스너.
 *
 * <p>Job 완료 시 수집 통계(발견 레포 수, 신규 커밋 수)를 저장하고,
 * 증분 수집 대상 유저에서 N일 연속 커밋 0건이 감지되면 Slack으로 알림을 보낸다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionResultListener implements JobExecutionListener {

    private static final int ANOMALY_THRESHOLD_DAYS = 3;

    private final DailyCollectionStatRepository dailyCollectionStatRepository;
    private final CollectionMetadataRepository collectionMetadataRepository;

    @Value("${slack.infra.webhook-url:}")
    private String slackWebhookUrl;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
            return;
        }

        Long userId = jobExecution.getJobParameters().getLong("userId");
        ExecutionContext context = jobExecution.getExecutionContext();

        boolean isIncremental = collectionMetadataRepository.findByUserId(userId).isPresent();
        int discoveredRepoCount = getDiscoveredRepoCount(context);
        int newCommitCount = getNewCommitCount(context);
        long durationMs = jobExecution.getEndTime() != null && jobExecution.getStartTime() != null
            ? jobExecution.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli()
                - jobExecution.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli()
            : 0;

        saveStat(userId, isIncremental, discoveredRepoCount, newCommitCount, durationMs);

        if (isIncremental) {
            checkAnomalyAndAlert(userId);
        }
    }

    private int getDiscoveredRepoCount(ExecutionContext context) {
        Object repos = context.get(StepContextKeys.DISCOVERED_REPOS);
        if (repos instanceof String[] repoArray) {
            return repoArray.length;
        }
        return 0;
    }

    private int getNewCommitCount(ExecutionContext context) {
        if (!context.containsKey(StepContextKeys.COMMIT_SAVED_COUNT)) {
            return 0;
        }
        return (int) context.getLong(StepContextKeys.COMMIT_SAVED_COUNT);
    }

    private void saveStat(Long userId, boolean isIncremental, int repoCount, int commitCount, long durationMs) {
        DailyCollectionStatDocument stat = DailyCollectionStatDocument.builder()
            .userId(userId)
            .collectionDate(LocalDate.now())
            .discoveredRepoCount(repoCount)
            .newCommitCount(commitCount)
            .batchDurationMs(durationMs)
            .incrementalCollection(isIncremental)
            .createdAt(Instant.now())
            .build();

        try {
            dailyCollectionStatRepository.save(stat);
            log.info("Daily collection stat saved — user: {}, repos: {}, newCommits: {}, incremental: {}, durationMs: {}",
                userId, repoCount, commitCount, isIncremental, durationMs);
        } catch (Exception e) {
            log.warn("Failed to save daily collection stat for user {}: {}", userId, e.getMessage());
        }
    }

    private void checkAnomalyAndAlert(Long userId) {
        LocalDate fromDate = LocalDate.now().minusDays(ANOMALY_THRESHOLD_DAYS - 1);
        List<DailyCollectionStatDocument> recentStats =
            dailyCollectionStatRepository
                .findByUserIdAndCollectionDateGreaterThanEqualAndIncrementalCollection(userId, fromDate, true);

        if (recentStats.size() < ANOMALY_THRESHOLD_DAYS) {
            return;
        }

        boolean allZeroCommits = recentStats.stream().allMatch(s -> s.getNewCommitCount() == 0);
        boolean allHaveRepos = recentStats.stream().allMatch(s -> s.getDiscoveredRepoCount() > 0);

        if (allZeroCommits && allHaveRepos) {
            log.warn("Commit collection anomaly detected for user {} — 0 new commits for {} consecutive days", userId, ANOMALY_THRESHOLD_DAYS);
            sendSlackAlert(userId);
        }
    }

    private void sendSlackAlert(Long userId) {
        if (slackWebhookUrl == null || slackWebhookUrl.isBlank()) {
            log.warn("Slack webhook URL not configured — skipping anomaly alert for user {}", userId);
            return;
        }

        String message = String.format(
            ":warning: *[KOSP Harvester] 커밋 수집 이상 감지*\\n"
                + "유저 ID: %d\\n"
                + "%d일 연속으로 증분 수집 대상 유저의 신규 커밋이 0건입니다.\\n"
                + "GitHub API 상태 및 수집 로직을 확인해주세요.",
            userId, ANOMALY_THRESHOLD_DAYS
        );

        try {
            String payload = "{\"text\": \"" + message + "\"}";
            URL url = new URL(slackWebhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.warn("Slack alert failed with response code {} for user {}", responseCode, userId);
            }
        } catch (Exception e) {
            log.warn("Failed to send Slack anomaly alert for user {}: {}", userId, e.getMessage());
        }
    }
}
