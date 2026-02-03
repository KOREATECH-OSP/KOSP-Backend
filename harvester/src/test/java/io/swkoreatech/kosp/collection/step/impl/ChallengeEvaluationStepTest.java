package io.swkoreatech.kosp.collection.step.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.common.event.ChallengeEvaluationRequest;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import io.swkoreatech.kosp.job.StepCompletionListener;

@DisplayName("ChallengeEvaluationStep 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChallengeEvaluationStepTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private StepCompletionListener stepCompletionListener;

    @InjectMocks
    private ChallengeEvaluationStep challengeEvaluationStep;

    @Nested
    @DisplayName("getStepName 메서드")
    class GetStepNameTest {

        @Test
        @DisplayName("올바른 스텝 이름을 반환한다")
        void returnsCorrectStepName() {
            String stepName = challengeEvaluationStep.getStepName();

            assertThat(stepName).isEqualTo("challengeEvaluationStep");
        }
    }

    @Nested
    @DisplayName("getStep 메서드")
    class GetStepTest {

        @Test
        @DisplayName("Step 인스턴스를 반환한다")
        void returnsStepInstance() {
            Step step = challengeEvaluationStep.getStep();

            assertThat(step).isNotNull();
        }
    }

    @Nested
    @DisplayName("publishChallengeEvaluationRequest 동작")
    class PublishEventTest {

        @Test
        @DisplayName("userId가 있을 때 RabbitMQ에 이벤트를 발행한다")
        void publishesEvent_whenUserIdExists() throws Exception {
            Long userId = 1L;
            ChallengeEvaluationRequest request = new ChallengeEvaluationRequest(
                userId,
                "test-message-id",
                java.time.LocalDateTime.now()
            );

            // Execute the private method via reflection (simulating tasklet execution)
            java.lang.reflect.Method method = ChallengeEvaluationStep.class.getDeclaredMethod(
                "publishChallengeEvaluationRequest", Long.class);
            method.setAccessible(true);
            method.invoke(challengeEvaluationStep, userId);

            // Verify RabbitMQ publish was called
            verify(rabbitTemplate).convertAndSend(
                eq(QueueNames.CHALLENGE_EVALUATION),
                any(ChallengeEvaluationRequest.class)
            );
        }

        @Test
        @DisplayName("발행된 이벤트에 userId가 포함된다")
        void eventContainsUserId() throws Exception {
            Long userId = 42L;
            ArgumentCaptor<ChallengeEvaluationRequest> requestCaptor = 
                ArgumentCaptor.forClass(ChallengeEvaluationRequest.class);

            // Execute the private method
            java.lang.reflect.Method method = ChallengeEvaluationStep.class.getDeclaredMethod(
                "publishChallengeEvaluationRequest", Long.class);
            method.setAccessible(true);
            method.invoke(challengeEvaluationStep, userId);

            // Verify and capture
            verify(rabbitTemplate).convertAndSend(
                eq(QueueNames.CHALLENGE_EVALUATION),
                requestCaptor.capture()
            );

            ChallengeEvaluationRequest request = requestCaptor.getValue();
            assertThat(request.userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("발행된 이벤트에 messageId가 포함된다")
        void eventContainsMessageId() throws Exception {
            Long userId = 1L;
            ArgumentCaptor<ChallengeEvaluationRequest> requestCaptor = 
                ArgumentCaptor.forClass(ChallengeEvaluationRequest.class);

            // Execute the private method
            java.lang.reflect.Method method = ChallengeEvaluationStep.class.getDeclaredMethod(
                "publishChallengeEvaluationRequest", Long.class);
            method.setAccessible(true);
            method.invoke(challengeEvaluationStep, userId);

            // Verify and capture
            verify(rabbitTemplate).convertAndSend(
                eq(QueueNames.CHALLENGE_EVALUATION),
                requestCaptor.capture()
            );

            ChallengeEvaluationRequest request = requestCaptor.getValue();
            assertThat(request.messageId()).isNotNull();
            assertThat(request.messageId()).isNotEmpty();
        }

        @Test
        @DisplayName("발행된 이벤트에 timestamp가 포함된다")
        void eventContainsTimestamp() throws Exception {
            Long userId = 1L;
            ArgumentCaptor<ChallengeEvaluationRequest> requestCaptor = 
                ArgumentCaptor.forClass(ChallengeEvaluationRequest.class);

            // Execute the private method
            java.lang.reflect.Method method = ChallengeEvaluationStep.class.getDeclaredMethod(
                "publishChallengeEvaluationRequest", Long.class);
            method.setAccessible(true);
            method.invoke(challengeEvaluationStep, userId);

            // Verify and capture
            verify(rabbitTemplate).convertAndSend(
                eq(QueueNames.CHALLENGE_EVALUATION),
                requestCaptor.capture()
            );

            ChallengeEvaluationRequest request = requestCaptor.getValue();
            assertThat(request.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("다양한 userId로 이벤트를 발행한다")
        void publishesEvent_withDifferentUserIds() throws Exception {
            Long userId = 999L;
            ArgumentCaptor<ChallengeEvaluationRequest> requestCaptor = 
                ArgumentCaptor.forClass(ChallengeEvaluationRequest.class);

            // Execute the private method
            java.lang.reflect.Method method = ChallengeEvaluationStep.class.getDeclaredMethod(
                "publishChallengeEvaluationRequest", Long.class);
            method.setAccessible(true);
            method.invoke(challengeEvaluationStep, userId);

            // Verify and capture
            verify(rabbitTemplate).convertAndSend(
                eq(QueueNames.CHALLENGE_EVALUATION),
                requestCaptor.capture()
            );

            ChallengeEvaluationRequest request = requestCaptor.getValue();
            assertThat(request.userId()).isEqualTo(999L);
        }

        @Test
        @DisplayName("올바른 queue name으로 발행한다")
        void publishesToCorrectQueue() throws Exception {
            Long userId = 1L;

            // Execute the private method
            java.lang.reflect.Method method = ChallengeEvaluationStep.class.getDeclaredMethod(
                "publishChallengeEvaluationRequest", Long.class);
            method.setAccessible(true);
            method.invoke(challengeEvaluationStep, userId);

            // Verify queue name
            verify(rabbitTemplate).convertAndSend(
                eq(QueueNames.CHALLENGE_EVALUATION),
                any(ChallengeEvaluationRequest.class)
            );
        }
    }

    private ChunkContext createMockChunkContext(Long userId, Long jobExecutionId) {
        ChunkContext chunkContext = mock(ChunkContext.class);
        StepContext stepContext = mock(StepContext.class);
        StepExecution stepExecution = mock(StepExecution.class);
        JobExecution jobExecution = mock(JobExecution.class);
        JobParameters jobParameters = mock(JobParameters.class);

        lenient().when(chunkContext.getStepContext()).thenReturn(stepContext);
        lenient().when(stepContext.getStepExecution()).thenReturn(stepExecution);
        lenient().when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        lenient().when(stepExecution.getJobExecutionId()).thenReturn(jobExecutionId);
        lenient().when(stepExecution.getJobParameters()).thenReturn(jobParameters);
        lenient().when(jobParameters.getLong("userId")).thenReturn(userId);

        return chunkContext;
    }
}
