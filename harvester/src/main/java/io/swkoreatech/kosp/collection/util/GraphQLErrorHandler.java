package io.swkoreatech.kosp.collection.util;

import java.util.List;
import java.util.Map;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * GraphQL 응답 에러를 표준화된 로깅으로 처리하는 유틸리티 클래스.
 */
@Slf4j
public final class GraphQLErrorHandler {

    private GraphQLErrorHandler() {
        throw new AssertionError("Utility class");
    }

    /**
     * GraphQL 응답 에러를 분류하여 처리 가능한 카테고리로 반환한다.
     *
     * <p>에러가 없으면 null을 반환한다. 부분 에러(데이터 존재)는 WARN 로그를 남긴다.
     * 전체 에러(데이터 없음)는 로그를 남기지 않고 호출자가 에러 타입에 따라 결정한다.
     *
     * @param response   GraphQL 응답 (nullable)
     * @param entityType 조회 대상 엔티티 타입 (예: "repo", "user")
     * @param entityId   엔티티 식별자 (예: "owner/name", "login")
     * @return 에러가 존재하면 GraphQLErrorType, 아니면 null
     */
    public static GraphQLErrorType classifyErrors(GraphQLResponse<?> response, String entityType, String entityId) {
        if (response == null) {
            log.warn("No response from GraphQL for {} {}", entityType, entityId);
            return GraphQLErrorType.RETRYABLE;
        }
        if (!response.hasErrors()) {
            return null;
        }
        if (response.getData() != null) {
            log.warn("Partial GraphQL errors for {} {} (data available, continuing): {}",
                    entityType, entityId, response.getErrors());
            return GraphQLErrorType.PARTIAL;
        }
        String errorMessage = extractErrorMessage(response.getErrors());
        if (errorMessage.startsWith("Something went wrong")) {
            return GraphQLErrorType.NON_RETRYABLE;
        }
        return GraphQLErrorType.RETRYABLE;
    }

    private static String extractErrorMessage(List<Map<String, Object>> errors) {
        if (errors == null || errors.isEmpty()) {
            return "";
        }
        Object message = errors.get(0).get("message");
        return message != null ? (String) message : "";
    }
}
