package io.swkoreatech.kosp.collection.util;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import io.swkoreatech.kosp.client.dto.GraphQLResponse;

/**
 * Utility class for handling GraphQL response errors with standardized logging.
 */
@Slf4j
public final class GraphQLErrorHandler {

    private GraphQLErrorHandler() {
        throw new AssertionError("Utility class");
    }

    /**
     * Classifies GraphQL response errors into actionable categories.
     * 
     * Returns null if no errors. Returns GraphQLErrorType enum for error classification.
     * Logs WARN for partial errors (data present). Does NOT log for total errors
     * (caller decides log level based on error type).
     *
     * @param response the GraphQL response (nullable)
     * @param entityType the type of entity being queried (e.g., "repo", "user")
     * @param entityId the entity identifier (e.g., "owner/name", "login")
     * @return GraphQLErrorType if error present, null if no error
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
