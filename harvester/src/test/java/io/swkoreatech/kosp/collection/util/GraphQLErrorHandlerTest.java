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
    @DisplayName("logAndCheckErrors 메서드")
    class LogAndCheckErrorsTest {

        @Test
        @DisplayName("null 응답이면 true를 반환하고 warn 로그를 남긴다")
        void returnsTrue_whenResponseIsNull() {
            // when
            boolean result = GraphQLErrorHandler.logAndCheckErrors(null, "repo", "owner/name");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("응답에 에러가 있으면 true를 반환하고 error 로그를 남긴다")
        void returnsTrue_whenResponseHasErrors() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(
                    Map.of("message", "Field error"),
                    Map.of("message", "Query error")
                )
            );

            // when
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, "user", "octocat");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("응답에 에러가 없으면 false를 반환한다")
        void returnsFalse_whenResponseHasNoErrors() {
            // given
            GraphQLResponse<Object> response = createResponseWithoutErrors();

            // when
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, "repo", "owner/name");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("응답에 빈 에러 리스트가 있으면 false를 반환한다")
        void returnsFalse_whenResponseHasEmptyErrorList() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(List.of());

            // when
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, "issue", "123");

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"repo", "user", "issue", "pullRequest"})
        @DisplayName("다양한 entityType에 대해 정상 동작한다")
        void handlesVariousEntityTypes(String entityType) {
            // given
            GraphQLResponse<Object> response = createResponseWithoutErrors();

            // when
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, entityType, "test-id");

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"owner/repo", "user-123", "issue-456", "pr-789"})
        @DisplayName("다양한 entityId에 대해 정상 동작한다")
        void handlesVariousEntityIds(String entityId) {
            // given
            GraphQLResponse<Object> response = createResponseWithoutErrors();

            // when
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, "repo", entityId);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("단일 에러가 있는 응답을 처리한다")
        void handlesSingleError() {
            // given
            GraphQLResponse<Object> response = createResponseWithErrors(
                List.of(Map.of("message", "Single error"))
            );

            // when
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, "repo", "owner/name");

            // then
            assertThat(result).isTrue();
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
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, "user", "octocat");

            // then
            assertThat(result).isTrue();
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
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, "repo", "owner/name");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("응답 객체가 유효하고 에러가 없으면 false를 반환한다")
        void returnsFalse_whenResponseIsValidWithoutErrors() {
            // given
            GraphQLResponse<Object> response = new GraphQLResponse<>();

            // when
            boolean result = GraphQLErrorHandler.logAndCheckErrors(response, "repo", "owner/name");

            // then
            assertThat(result).isFalse();
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
