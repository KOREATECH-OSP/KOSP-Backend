package io.swkoreatech.kosp.job;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LoggingConstants 단위 테스트")
class LoggingConstantsTest {

    @Nested
    @DisplayName("상수 값 검증")
    class ConstantValueTest {

        @Test
        @DisplayName("모든 상수가 null이 아니다")
        void allConstants_areNotNull() {
            assertThat(LoggingConstants.JOB_STARTED).isNotNull();
            assertThat(LoggingConstants.JOB_FINISHED).isNotNull();
            assertThat(LoggingConstants.STEP_STARTED).isNotNull();
            assertThat(LoggingConstants.STEP_FINISHED).isNotNull();
            assertThat(LoggingConstants.STEP_METRICS).isNotNull();
            assertThat(LoggingConstants.MINING_PROGRESS).isNotNull();
            assertThat(LoggingConstants.MINING_SUMMARY).isNotNull();
        }

        @Test
        @DisplayName("모든 상수가 빈 문자열이 아니다")
        void allConstants_areNotEmpty() {
            assertThat(LoggingConstants.JOB_STARTED).isNotEmpty();
            assertThat(LoggingConstants.JOB_FINISHED).isNotEmpty();
            assertThat(LoggingConstants.STEP_STARTED).isNotEmpty();
            assertThat(LoggingConstants.STEP_FINISHED).isNotEmpty();
            assertThat(LoggingConstants.STEP_METRICS).isNotEmpty();
            assertThat(LoggingConstants.MINING_PROGRESS).isNotEmpty();
            assertThat(LoggingConstants.MINING_SUMMARY).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("SLF4J 플레이스홀더 검증")
    class Slf4jPlaceholderTest {

        @Test
        @DisplayName("모든 상수가 SLF4J 플레이스홀더를 포함한다")
        void allConstants_containSlf4jPlaceholders() {
            assertThat(LoggingConstants.JOB_STARTED).contains("{}");
            assertThat(LoggingConstants.JOB_FINISHED).contains("{}");
            assertThat(LoggingConstants.STEP_STARTED).contains("{}");
            assertThat(LoggingConstants.STEP_FINISHED).contains("{}");
            assertThat(LoggingConstants.STEP_METRICS).contains("{}");
            assertThat(LoggingConstants.MINING_PROGRESS).contains("{}");
            assertThat(LoggingConstants.MINING_SUMMARY).contains("{}");
        }

        @Test
        @DisplayName("JOB_STARTED는 1개의 플레이스홀더를 포함한다")
        void jobStarted_containsOneePlaceholder() {
            int count = countPlaceholders(LoggingConstants.JOB_STARTED);
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("JOB_FINISHED는 2개의 플레이스홀더를 포함한다")
        void jobFinished_containsTwoPlaceholders() {
            int count = countPlaceholders(LoggingConstants.JOB_FINISHED);
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("STEP_STARTED는 2개의 플레이스홀더를 포함한다")
        void stepStarted_containsTwoPlaceholders() {
            int count = countPlaceholders(LoggingConstants.STEP_STARTED);
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("STEP_FINISHED는 3개의 플레이스홀더를 포함한다")
        void stepFinished_containsThreePlaceholders() {
            int count = countPlaceholders(LoggingConstants.STEP_FINISHED);
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("STEP_METRICS는 5개의 플레이스홀더를 포함한다")
        void stepMetrics_containsFivePlaceholders() {
            int count = countPlaceholders(LoggingConstants.STEP_METRICS);
            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("MINING_PROGRESS는 2개의 플레이스홀더를 포함한다")
        void miningProgress_containsTwoPlaceholders() {
            int count = countPlaceholders(LoggingConstants.MINING_PROGRESS);
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("MINING_SUMMARY는 3개의 플레이스홀더를 포함한다")
        void miningSummary_containsThreePlaceholders() {
            int count = countPlaceholders(LoggingConstants.MINING_SUMMARY);
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("하드코딩된 값 검증")
    class NoHardcodedValuesTest {

        @Test
        @DisplayName("상수에 숫자 값이 포함되지 않는다")
        void constants_doNotContainNumericValues() {
            assertThat(LoggingConstants.JOB_STARTED).doesNotContainPattern("\\d+");
            assertThat(LoggingConstants.JOB_FINISHED).doesNotContainPattern("\\d+");
            assertThat(LoggingConstants.STEP_STARTED).doesNotContainPattern("\\d+");
            assertThat(LoggingConstants.STEP_FINISHED).doesNotContainPattern("\\d+");
            assertThat(LoggingConstants.STEP_METRICS).doesNotContainPattern("\\d+");
            assertThat(LoggingConstants.MINING_PROGRESS).doesNotContainPattern("\\d+");
            assertThat(LoggingConstants.MINING_SUMMARY).doesNotContainPattern("\\d+");
        }

        @Test
        @DisplayName("상수에 실제 사용자 ID가 포함되지 않는다")
        void constants_doNotContainActualUserIds() {
            String[] constants = {
                LoggingConstants.JOB_STARTED,
                LoggingConstants.JOB_FINISHED,
                LoggingConstants.STEP_STARTED,
                LoggingConstants.STEP_FINISHED,
                LoggingConstants.STEP_METRICS,
                LoggingConstants.MINING_PROGRESS,
                LoggingConstants.MINING_SUMMARY
            };

            for (String constant : constants) {
                assertThat(constant).doesNotContainPattern("user\\d+");
                assertThat(constant).doesNotContainPattern("job\\d+");
                assertThat(constant).doesNotContainPattern("step\\d+");
            }
        }

        @Test
        @DisplayName("상수에 실제 데이터 값이 포함되지 않는다")
        void constants_doNotContainActualDataValues() {
            assertThat(LoggingConstants.JOB_STARTED).doesNotContain("SUCCESS");
            assertThat(LoggingConstants.JOB_STARTED).doesNotContain("FAILED");
            assertThat(LoggingConstants.JOB_FINISHED).doesNotContain("SUCCESS");
            assertThat(LoggingConstants.JOB_FINISHED).doesNotContain("FAILED");
            assertThat(LoggingConstants.STEP_METRICS).doesNotContain("1000");
            assertThat(LoggingConstants.STEP_METRICS).doesNotContain("500");
        }
    }

    private int countPlaceholders(String constant) {
        int count = 0;
        int index = 0;
        while ((index = constant.indexOf("{}", index)) != -1) {
            count++;
            index += 2;
        }
        return count;
    }
}
