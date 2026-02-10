package io.swkoreatech.kosp.collection.util;

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
     * Logs GraphQL errors if present and distinguishes partial from total errors.
     * 
     * Partial errors: data is present despite errors (e.g., SERVICE_UNAVAILABLE on specific fields).
     * Logs at WARN level and returns false to continue processing with available data.
     * 
     * Total errors: data is null, indicating complete query failure (e.g., huge repo timeout).
     * Logs at ERROR level and returns true to abort and trigger retry logic.
     *
     * @param response the GraphQL response (nullable)
     * @param entityType the type of entity being queried (e.g., "repo", "user")
     * @param entityId the entity identifier (e.g., "owner/name", "login")
     * @return true if total error (data absent), false if partial error or no error
     */
    public static boolean logAndCheckErrors(GraphQLResponse<?> response, String entityType, String entityId) {
        if (response == null) {
            log.warn("No response from GraphQL for {} {}", entityType, entityId);
            return true;
        }
        if (response.hasErrors()) {
            if (response.getData() != null) {
                log.warn("Partial GraphQL errors for {} {} (data available, continuing): {}",
                        entityType, entityId, response.getErrors());
                return false;
            }
            log.error("GraphQL errors for {} {}: {}", entityType, entityId, response.getErrors());
            return true;
        }
        return false;
    }
}
