package io.swkoreatech.kosp.collection.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("NullSafeGetters 단위 테스트")
class NullSafeGettersTest {

    @Nested
    @DisplayName("intOrZero 메서드")
    class IntOrZeroTest {

        @ParameterizedTest
        @NullSource
        @DisplayName("null이면 0을 반환한다")
        void returnsZero_whenNull(Integer value) {
            // when
            int result = NullSafeGetters.intOrZero(value);

            // then
            assertThat(result).isZero();
        }

        @ParameterizedTest
        @ValueSource(ints = {42, 0, -1, 100, Integer.MAX_VALUE})
        @DisplayName("null이 아니면 해당 값을 반환한다")
        void returnsValue_whenNotNull(int value) {
            // when
            int result = NullSafeGetters.intOrZero(value);

            // then
            assertThat(result).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("longOrZero 메서드")
    class LongOrZeroTest {

        @ParameterizedTest
        @NullSource
        @DisplayName("null이면 0L을 반환한다")
        void returnsZero_whenNull(Long value) {
            // when
            long result = NullSafeGetters.longOrZero(value);

            // then
            assertThat(result).isZero();
        }

        @ParameterizedTest
        @ValueSource(longs = {100L, 0L, -1L, 999999L, Long.MAX_VALUE})
        @DisplayName("null이 아니면 해당 값을 반환한다")
        void returnsValue_whenNotNull(long value) {
            // when
            long result = NullSafeGetters.longOrZero(value);

            // then
            assertThat(result).isEqualTo(value);
        }
    }
}
