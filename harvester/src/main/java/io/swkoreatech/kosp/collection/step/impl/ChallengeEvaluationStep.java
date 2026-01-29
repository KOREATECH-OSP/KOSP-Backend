package io.swkoreatech.kosp.collection.step.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.job.StepCompletionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes challenge evaluation event to Redis stream after score calculation.
 *
 * @StepContract
 * REQUIRES: userId from job parameters, score calculation completed
 * PROVIDES: Event published to kosp:challenge-check stream
 * PURPOSE: Notifies backend module to check user challenges and award points
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeEvaluationStep implements StepProvider {

    private static final String STEP_NAME = "challengeEvaluationStep";
    private static final String STREAM_KEY = "kosp:challenge-check";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StringRedisTemplate redisTemplate;
    private final StepCompletionListener stepCompletionListener;

    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = StepContextHelper.extractUserId(chunkContext);
                if (userId == null) {
                    return RepeatStatus.FINISHED;
                }
                publishEvent(userId, chunkContext);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .listener(stepCompletionListener)
            .build();
    }

    @Override
    public String getStepName() {
        return STEP_NAME;
    }

    private void publishEvent(Long userId, ChunkContext chunkContext) {
        Long jobExecutionId = extractJobExecutionId(chunkContext);
        String calculatedAt = Instant.now().toString();

        Map<String, String> payload = buildPayload(userId, jobExecutionId, calculatedAt);

        redisTemplate.opsForStream().add(STREAM_KEY, payload);

        log.info("Published challenge evaluation event for userId={}, jobExecutionId={}",
            userId, jobExecutionId);
    }

    private Long extractJobExecutionId(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecutionId();
    }

    private Map<String, String> buildPayload(Long userId, Long jobExecutionId, String calculatedAt) {
        Map<String, String> payload = new HashMap<>();
        payload.put("userId", String.valueOf(userId));
        payload.put("jobExecutionId", String.valueOf(jobExecutionId));
        payload.put("calculatedAt", calculatedAt);
        return payload;
    }
}
