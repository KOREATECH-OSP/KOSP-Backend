package io.swkoreatech.kosp.collection.util;

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
     * GraphQL 에러가 존재하면 로그를 기록하고 에러 존재 여부를 반환한다.
     *
     * @param response   GraphQL 응답 (nullable)
     * @param entityType 조회 대상 엔티티 타입 (예: "repo", "user")
     * @param entityId   엔티티 식별자 (예: "owner/name", "login")
     * @return 에러가 존재하면 true, 아니면 false
     */
    public static boolean logAndCheckErrors(GraphQLResponse<?> response, String entityType, String entityId) {
        if (response == null) {
            log.warn("No response from GraphQL for {} {}", entityType, entityId);
            return true;
        }
        if (response.hasErrors()) {
            log.error("GraphQL errors for {} {}: {}", entityType, entityId, response.getErrors());
            return true;
        }
        return false;
    }
}
