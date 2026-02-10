package io.swkoreatech.kosp.collection.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;

@DisplayName("GraphQLErrorHandler 단위 테스트")
class GraphQLErrorHandlerTest {

    @Nested
    @DisplayName("classifyErrors 메서드 - 기본 동작")
    class ClassifyErrorsBasicTest {

        @Test
        @DisplayName("null 응답이면 RETRYABLE을 반환한다")
        void returnsRETRYABLE_whenResponseIsNull() {
            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(null, "repo", "owner/name");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.RETRYABLE);
        }

        @Test
        @DisplayName("응답에 에러가 있으면 에러 타입을 반환한다")
        void returnsErrorType_whenResponseHasErrors() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(
                    Map.of("message", "Field error"),
                    Map.of("message", "Query error")
                )
            );

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "user", "octocat");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.RETRYABLE);
        }

        @Test
        @DisplayName("응답에 에러가 없으면 null을 반환한다")
        void returnsNull_whenResponseHasNoErrors() {
            // given
            GraphQLResponse<Object> response = createResponseWithoutErrors();

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "owner/name");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("응답에 빈 에러 리스트가 있으면 null을 반환한다")
        void returnsNull_whenResponseHasEmptyErrorList() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(List.of());

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "issue", "123");

            // then
            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"repo", "user", "issue", "pullRequest"})
        @DisplayName("다양한 entityType에 대해 정상 동작한다")
        void handlesVariousEntityTypes(String entityType) {
            // given
            GraphQLResponse<Object> response = createResponseWithoutErrors();

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, entityType, "test-id");

            // then
            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"owner/repo", "user-123", "issue-456", "pr-789"})
        @DisplayName("다양한 entityId에 대해 정상 동작한다")
        void handlesVariousEntityIds(String entityId) {
            // given
            GraphQLResponse<Object> response = createResponseWithoutErrors();

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", entityId);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("단일 에러가 있는 응답을 처리한다")
        void handlesSingleError() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(Map.of("message", "Single error"))
            );

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "owner/name");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.RETRYABLE);
        }

        @Test
        @DisplayName("여러 에러가 있는 응답을 처리한다")
        void handlesMultipleErrors() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(
                    Map.of("message", "Error 1", "type", "QUERY_ERROR"),
                    Map.of("message", "Error 2", "type", "FIELD_ERROR"),
                    Map.of("message", "Error 3", "type", "VALIDATION_ERROR")
                )
            );

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "user", "octocat");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.RETRYABLE);
        }

        @Test
        @DisplayName("에러 메시지에 특수 문자가 포함되어 있어도 처리한다")
        void handlesErrorsWithSpecialCharacters() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(
                    Map.of("message", "Error with \"quotes\" and 'apostrophes'"),
                    Map.of("message", "Error with {braces} and [brackets]")
                )
            );

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "owner/name");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.RETRYABLE);
        }

        @Test
        @DisplayName("응답 객체가 유효하고 에러가 없으면 null을 반환한다")
        void returnsNull_whenResponseIsValidWithoutErrors() {
            // given
            GraphQLResponse<Object> response = new GraphQLResponse<>();

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "owner/name");

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("classifyErrors 메서드 - 에러 타입 분류")
    class ClassifyErrorsTypeTest {

        @Test
        @DisplayName("에러 없음 → null 반환")
        void noError_returnsNull() {
            // given
            GraphQLResponse<Object> response = createResponseWithoutErrors();

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "test");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("데이터 있고 에러 있음 (partial) → PARTIAL 반환")
        void partialError_returnsPARTIAL() {
            // given
            GraphQLResponse<Object> response = createResponseWithPartialError();

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "test");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.PARTIAL);
        }

        @Test
        @DisplayName("Something went wrong 메시지 → NON_RETRYABLE 반환")
        void somethingWentWrong_returnsNON_RETRYABLE() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(Map.of("message", "Something went wrong while processing"))
            );

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "test");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.NON_RETRYABLE);
        }

        @Test
        @DisplayName("Something went wrong (정확한 일치) → NON_RETRYABLE 반환")
        void somethingWentWrongExact_returnsNON_RETRYABLE() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(Map.of("message", "Something went wrong"))
            );

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "test");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.NON_RETRYABLE);
        }

        @Test
        @DisplayName("다른 에러 메시지 (total error) → RETRYABLE 반환")
        void otherTotalError_returnsRETRYABLE() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(Map.of("message", "Rate limit exceeded"))
            );

            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(response, "repo", "test");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.RETRYABLE);
        }

        @Test
        @DisplayName("null 응답 → RETRYABLE 반환")
        void nullResponse_returnsRETRYABLE() {
            // when
            GraphQLErrorType result = GraphQLErrorHandler.classifyErrors(null, "repo", "test");

            // then
            assertThat(result).isEqualTo(GraphQLErrorType.RETRYABLE);
        }
    }

    private GraphQLResponse<Object> createResponseWithErrors(List<Map<String, Object>> errors) {
        GraphQLResponse<Object> response = new GraphQLResponse<>();
        setFieldValue(response, "errors", errors);
        return response;
    }

    private GraphQLResponse<Object> createResponseWithoutErrors() {
        GraphQLResponse<Object> response = new GraphQLResponse<>();
        setFieldValue(response, "errors", null);
        return response;
    }

    private GraphQLResponse<Object> createResponseWithPartialError() {
        GraphQLResponse<Object> response = new GraphQLResponse<>();
        setFieldValue(response, "data", new Object());
        setFieldValue(response, "errors", List.of(Map.of("message", "Partial error")));
        return response;
    }

    private void setFieldValue(GraphQLResponse<Object> response, String fieldName, Object value) {
        try {
            var field = GraphQLResponse.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(response, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
