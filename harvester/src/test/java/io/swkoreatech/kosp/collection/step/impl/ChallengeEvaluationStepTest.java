package io.swkoreatech.kosp.collection.step.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.job.StepCompletionListener;

@DisplayName("ChallengeEvaluationStep 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChallengeEvaluationStepTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private StringRedisTemplate redisTemplate;

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
    @DisplayName("publishEvent 동작")
    class PublishEventTest {

        @Test
        @DisplayName("userId가 있을 때 Redis stream에 이벤트를 발행한다")
        void publishesEvent_whenUserIdExists() throws Exception {
            StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
            when(redisTemplate.opsForStream()).thenReturn(streamOps);

            ChunkContext chunkContext = createMockChunkContext(1L, 100L);
            invokePublishEvent(1L, chunkContext);

            verify(redisTemplate).opsForStream();
            verify(streamOps).add(eq("kosp:challenge-check"), any(Map.class));
        }

        @Test
        @DisplayName("발행된 이벤트에 userId가 포함된다")
        void eventContainsUserId() throws Exception {
            StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
            when(redisTemplate.opsForStream()).thenReturn(streamOps);
            ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);

            ChunkContext chunkContext = createMockChunkContext(1L, 100L);
            invokePublishEvent(1L, chunkContext);

            verify(streamOps).add(eq("kosp:challenge-check"), payloadCaptor.capture());
            Map<String, String> payload = payloadCaptor.getValue();
            assertThat(payload.get("userId")).isEqualTo("1");
        }

        @Test
        @DisplayName("발행된 이벤트에 jobExecutionId가 포함된다")
        void eventContainsJobExecutionId() throws Exception {
            StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
            when(redisTemplate.opsForStream()).thenReturn(streamOps);
            ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);

            ChunkContext chunkContext = createMockChunkContext(1L, 100L);
            invokePublishEvent(1L, chunkContext);

            verify(streamOps).add(eq("kosp:challenge-check"), payloadCaptor.capture());
            Map<String, String> payload = payloadCaptor.getValue();
            assertThat(payload.get("jobExecutionId")).isEqualTo("100");
        }

        @Test
        @DisplayName("발행된 이벤트에 calculatedAt이 포함된다")
        void eventContainsCalculatedAt() throws Exception {
            StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
            when(redisTemplate.opsForStream()).thenReturn(streamOps);
            ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);

            ChunkContext chunkContext = createMockChunkContext(1L, 100L);
            invokePublishEvent(1L, chunkContext);

            verify(streamOps).add(eq("kosp:challenge-check"), payloadCaptor.capture());
            Map<String, String> payload = payloadCaptor.getValue();
            assertThat(payload.get("calculatedAt")).isNotNull();
            assertThat(payload.get("calculatedAt")).contains("T");
        }

        @Test
        @DisplayName("다양한 userId로 이벤트를 발행한다")
        void publishesEvent_withDifferentUserIds() throws Exception {
            StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
            when(redisTemplate.opsForStream()).thenReturn(streamOps);
            ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);

            ChunkContext chunkContext = createMockChunkContext(999L, 200L);
            invokePublishEvent(999L, chunkContext);

            verify(streamOps).add(eq("kosp:challenge-check"), payloadCaptor.capture());
            Map<String, String> payload = payloadCaptor.getValue();
            assertThat(payload.get("userId")).isEqualTo("999");
            assertThat(payload.get("jobExecutionId")).isEqualTo("200");
        }

        private void invokePublishEvent(Long userId, ChunkContext chunkContext) throws Exception {
            Method method = ChallengeEvaluationStep.class.getDeclaredMethod(
                "publishEvent", Long.class, ChunkContext.class);
            method.setAccessible(true);
            method.invoke(challengeEvaluationStep, userId, chunkContext);
        }
    }

    @Nested
    @DisplayName("buildPayload 동작")
    class BuildPayloadTest {

        @Test
        @DisplayName("올바른 형식의 payload를 생성한다")
        void buildsCorrectPayload() throws Exception {
            Long userId = 42L;
            Long jobExecutionId = 123L;
            String calculatedAt = "2024-01-01T00:00:00Z";

            Map<String, String> payload = invokeBuildPayload(userId, jobExecutionId, calculatedAt);

            assertThat(payload).hasSize(3);
            assertThat(payload.get("userId")).isEqualTo("42");
            assertThat(payload.get("jobExecutionId")).isEqualTo("123");
            assertThat(payload.get("calculatedAt")).isEqualTo("2024-01-01T00:00:00Z");
        }

        private Map<String, String> invokeBuildPayload(Long userId, Long jobExecutionId, String calculatedAt) throws Exception {
            Method method = ChallengeEvaluationStep.class.getDeclaredMethod(
                "buildPayload", Long.class, Long.class, String.class);
            method.setAccessible(true);
            return (Map<String, String>) method.invoke(challengeEvaluationStep, userId, jobExecutionId, calculatedAt);
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