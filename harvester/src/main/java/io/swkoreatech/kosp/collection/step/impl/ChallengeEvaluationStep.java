package io.swkoreatech.kosp.collection.step.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.common.event.ChallengeEvaluationRequest;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import io.swkoreatech.kosp.job.StepCompletionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes challenge evaluation event to RabbitMQ after score calculation.
 *
 * @StepContract
 * REQUIRES: userId from job parameters, score calculation completed
 * PROVIDES: Event published to challenge evaluation queue
 * PURPOSE: Notifies challenge-service to check user challenges and award points
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeEvaluationStep implements StepProvider {

    private static final String STEP_NAME = "challengeEvaluationStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RabbitTemplate rabbitTemplate;
    private final StepCompletionListener stepCompletionListener;

    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = StepContextHelper.extractUserId(chunkContext);
                if (userId == null) {
                    return RepeatStatus.FINISHED;
                }
                publishChallengeEvaluationRequest(userId);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .listener(stepCompletionListener)
            .build();
    }

    @Override
    public String getStepName() {
        return STEP_NAME;
    }

    private void publishChallengeEvaluationRequest(Long userId) {
        ChallengeEvaluationRequest request = new ChallengeEvaluationRequest(
            userId,
            UUID.randomUUID().toString(),
            LocalDateTime.now()
        );
        
        rabbitTemplate.convertAndSend(
            QueueNames.CHALLENGE_EVALUATION,
            request
        );

        log.info("Published challenge evaluation request for userId={}", userId);
    }
}
