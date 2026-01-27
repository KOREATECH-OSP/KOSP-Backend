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
     * Logs GraphQL errors if present and returns whether errors exist.
     *
     * @param response the GraphQL response (nullable)
     * @param entityType the type of entity being queried (e.g., "repo", "user")
     * @param entityId the entity identifier (e.g., "owner/name", "login")
     * @return true if errors exist, false otherwise
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
