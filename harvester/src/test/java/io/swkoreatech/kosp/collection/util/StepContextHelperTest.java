package io.swkoreatech.kosp.collection.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;

@DisplayName("StepContextHelper 단위 테스트")
class StepContextHelperTest {

    private ChunkContext createMockChunkContext(ExecutionContext executionContext, Long userId) {
        ChunkContext chunkContext = mock(ChunkContext.class);
        StepContext stepContext = mock(StepContext.class);
        StepExecution stepExecution = mock(StepExecution.class);
        JobExecution jobExecution = mock(JobExecution.class);
        JobParameters jobParameters = mock(JobParameters.class);

        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        when(stepExecution.getJobParameters()).thenReturn(jobParameters);
        when(jobParameters.getLong("userId")).thenReturn(userId);

        return chunkContext;
    }

    @Nested
    @DisplayName("getExecutionContext 메서드")
    class GetExecutionContextTest {

        @Test
        @DisplayName("ChunkContext에서 ExecutionContext를 정상적으로 추출한다")
        void extractsExecutionContext_successfully() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putString("testKey", "testValue");
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            ExecutionContext result = StepContextHelper.getExecutionContext(chunkContext);

            // then
            assertThat(result).isNotNull();
            assertThat(result.get("testKey")).isEqualTo("testValue");
        }

        @Test
        @DisplayName("빈 ExecutionContext를 반환한다")
        void returnsEmptyExecutionContext() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            ExecutionContext result = StepContextHelper.getExecutionContext(chunkContext);

            // then
            assertThat(result).isNotNull();
            assertThat(result.size()).isZero();
        }

        @Test
        @DisplayName("여러 값이 포함된 ExecutionContext를 반환한다")
        void returnsExecutionContext_withMultipleValues() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putString("key1", "value1");
            executionContext.putString("key2", "value2");
            executionContext.putLong("key3", 123L);
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            ExecutionContext result = StepContextHelper.getExecutionContext(chunkContext);

            // then
            assertThat(result).isNotNull();
            assertThat(result.get("key1")).isEqualTo("value1");
            assertThat(result.get("key2")).isEqualTo("value2");
            assertThat(result.get("key3")).isEqualTo(123L);
        }
    }

    @Nested
    @DisplayName("extractUserId 메서드")
    class ExtractUserIdTest {

        @Test
        @DisplayName("JobParameters에서 userId를 정상적으로 추출한다")
        void extractsUserId_successfully() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            Long expectedUserId = 42L;
            ChunkContext chunkContext = createMockChunkContext(executionContext, expectedUserId);

            // when
            Long result = StepContextHelper.extractUserId(chunkContext);

            // then
            assertThat(result).isEqualTo(expectedUserId);
        }

        @Test
        @DisplayName("다양한 userId 값을 정상적으로 추출한다")
        void extractsVariousUserIds() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            Long userId = 999L;
            ChunkContext chunkContext = createMockChunkContext(executionContext, userId);

            // when
            Long result = StepContextHelper.extractUserId(chunkContext);

            // then
            assertThat(result).isEqualTo(999L);
        }

        @Test
        @DisplayName("userId가 1일 때 정상적으로 추출한다")
        void extractsUserId_whenValueIsOne() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            Long result = StepContextHelper.extractUserId(chunkContext);

            // then
            assertThat(result).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("extractString 메서드")
    class ExtractStringTest {

        @Test
        @DisplayName("존재하는 키에서 String 값을 정상적으로 추출한다")
        void extractsString_whenKeyExists() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putString("repositoryUrl", "https://github.com/user/repo");
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            String result = StepContextHelper.extractString(chunkContext, "repositoryUrl");

            // then
            assertThat(result).isEqualTo("https://github.com/user/repo");
        }

        @Test
        @DisplayName("존재하지 않는 키에 대해 null을 반환한다")
        void returnsNull_whenKeyDoesNotExist() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            String result = StepContextHelper.extractString(chunkContext, "nonExistentKey");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("값이 String이 아닐 때 null을 반환한다")
        void returnsNull_whenValueIsNotString() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putLong("numericKey", 12345L);
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            String result = StepContextHelper.extractString(chunkContext, "numericKey");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열을 정상적으로 추출한다")
        void extractsEmptyString() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putString("emptyKey", "");
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            String result = StepContextHelper.extractString(chunkContext, "emptyKey");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("특수 문자를 포함한 문자열을 정상적으로 추출한다")
        void extractsString_withSpecialCharacters() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            String specialString = "test@#$%^&*()_+-=[]{}|;:',.<>?/";
            executionContext.putString("specialKey", specialString);
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            String result = StepContextHelper.extractString(chunkContext, "specialKey");

            // then
            assertThat(result).isEqualTo(specialString);
        }

        @Test
        @DisplayName("여러 키 중에서 올바른 키의 값을 추출한다")
        void extractsCorrectString_fromMultipleKeys() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putString("key1", "value1");
            executionContext.putString("key2", "value2");
            executionContext.putString("key3", "value3");
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            String result = StepContextHelper.extractString(chunkContext, "key2");

            // then
            assertThat(result).isEqualTo("value2");
        }

        @Test
        @DisplayName("Integer 값이 저장된 키에 대해 null을 반환한다")
        void returnsNull_whenValueIsInteger() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put("intKey", 42);
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            String result = StepContextHelper.extractString(chunkContext, "intKey");

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("putString 메서드")
    class PutStringTest {

        @Test
        @DisplayName("ExecutionContext에 String 값을 정상적으로 저장한다")
        void putsString_successfully() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            StepContextHelper.putString(chunkContext, "testKey", "testValue");

            // then
            assertThat(executionContext.get("testKey")).isEqualTo("testValue");
        }

        @Test
        @DisplayName("여러 String 값을 순차적으로 저장한다")
        void putMultipleStrings_sequentially() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            StepContextHelper.putString(chunkContext, "key1", "value1");
            StepContextHelper.putString(chunkContext, "key2", "value2");
            StepContextHelper.putString(chunkContext, "key3", "value3");

            // then
            assertThat(executionContext.get("key1")).isEqualTo("value1");
            assertThat(executionContext.get("key2")).isEqualTo("value2");
            assertThat(executionContext.get("key3")).isEqualTo("value3");
        }

        @Test
        @DisplayName("빈 문자열을 저장한다")
        void putsEmptyString() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            StepContextHelper.putString(chunkContext, "emptyKey", "");

            // then
            assertThat(executionContext.get("emptyKey")).isEqualTo("");
        }

        @Test
        @DisplayName("특수 문자를 포함한 문자열을 저장한다")
        void putsString_withSpecialCharacters() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);
            String specialString = "https://github.com/user/repo?param=value&other=123";

            // when
            StepContextHelper.putString(chunkContext, "urlKey", specialString);

            // then
            assertThat(executionContext.get("urlKey")).isEqualTo(specialString);
        }

        @Test
        @DisplayName("기존 값을 새로운 값으로 덮어쓴다")
        void overwritesExistingValue() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putString("key", "oldValue");
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            StepContextHelper.putString(chunkContext, "key", "newValue");

            // then
            assertThat(executionContext.get("key")).isEqualTo("newValue");
        }

        @Test
        @DisplayName("null 값을 저장한다")
        void putsNullValue() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            StepContextHelper.putString(chunkContext, "nullKey", null);

            // then
            assertThat(executionContext.get("nullKey")).isNull();
        }

        @Test
        @DisplayName("긴 문자열을 저장한다")
        void putsLongString() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);
            String longString = "a".repeat(1000);

            // when
            StepContextHelper.putString(chunkContext, "longKey", longString);

            // then
            assertThat(executionContext.get("longKey")).isEqualTo(longString);
        }
    }

    @Nested
    @DisplayName("메서드 간 통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("putString으로 저장한 값을 extractString으로 추출한다")
        void putAndExtractString_integration() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);
            String testValue = "integration-test-value";

            // when
            StepContextHelper.putString(chunkContext, "integrationKey", testValue);
            String result = StepContextHelper.extractString(chunkContext, "integrationKey");

            // then
            assertThat(result).isEqualTo(testValue);
        }

        @Test
        @DisplayName("getExecutionContext로 얻은 컨텍스트에 putString으로 저장한다")
        void getExecutionContext_and_putString_integration() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            ChunkContext chunkContext = createMockChunkContext(executionContext, 1L);

            // when
            ExecutionContext context = StepContextHelper.getExecutionContext(chunkContext);
            StepContextHelper.putString(chunkContext, "key", "value");

            // then
            assertThat(context.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("extractUserId와 extractString을 함께 사용한다")
        void extractUserId_and_extractString_integration() {
            // given
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.putString("userName", "testUser");
            Long expectedUserId = 123L;
            ChunkContext chunkContext = createMockChunkContext(executionContext, expectedUserId);

            // when
            Long userId = StepContextHelper.extractUserId(chunkContext);
            String userName = StepContextHelper.extractString(chunkContext, "userName");

            // then
            assertThat(userId).isEqualTo(expectedUserId);
            assertThat(userName).isEqualTo("testUser");
        }
    }
}
