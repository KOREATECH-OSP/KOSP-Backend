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
 * 점수 계산 완료 후 챌린지 평가 이벤트를 RabbitMQ에 발행하는 스텝.
 *
 * <p>챌린지 서비스에 사용자 챌린지 확인 및 포인트 부여를 알린다.
 *
 * @StepContract
 * REQUIRES: 잡 파라미터의 userId, 점수 계산 완료
 * PROVIDES: 챌린지 평가 큐에 이벤트 발행
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
