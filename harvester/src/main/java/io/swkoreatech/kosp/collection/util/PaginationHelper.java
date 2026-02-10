package io.swkoreatech.kosp.collection.util;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility class for handling GraphQL pagination with generic data types.
 *
 * <p>This utility abstracts the common pagination pattern used across multiple mining steps
 * (PullRequestMiningStep, IssueMiningStep, CommitMiningStep). It manages cursor-based pagination
 * using a do-while loop and integrates with GraphQLErrorHandler for error checking.
 *
 * <p><b>Generic Type Parameters:</b>
 * <ul>
 *   <li>{@code T} - The data class type extracted from GraphQL response (e.g., UserPullRequestsResponse,
 *       UserIssuesResponse, RepositoryCommitsResponse). Must contain a PageInfo object for pagination metadata.</li>
 *   <li>{@code P} - The PageInfo type (inner class of T, e.g., UserPullRequestsResponse.PageInfo).</li>
 * </ul>
 *
 * <p><b>Parameters:</b>
 * <ul>
 *   <li>{@code fetcher} - Function that fetches a single page of data. Takes cursor (nullable) and returns
 *       GraphQLResponse containing data of type T. Responsible for making the actual GraphQL API call.</li>
 *   <li>{@code pageInfoExtractor} - Function that extracts PageInfo from the data object. Takes the deserialized
 *       data (type T) and returns its PageInfo for determining if more pages exist.</li>
 *   <li>{@code dataProcessor} - BiFunction that processes the fetched data and returns the count of items saved.
 *       Takes the data object (type T) and current cursor, returns count of saved items. Accumulates across pages.</li>
 *   <li>{@code entityType} - String identifier for the entity type being queried (e.g., "user", "repo").
 *       Used in error logging for context.</li>
 *   <li>{@code entityId} - String identifier for the specific entity (e.g., "octocat", "owner/repo").
 *       Used in error logging for context.</li>
 * </ul>
 *
 * <p><b>Return Value:</b>
 * <ul>
 *   <li>Total count of items saved across all pages. Returns 0 if errors occur on first page.</li>
 * </ul>
 *
 * <p><b>Example Usage:</b>
 * <pre>{@code
 * private int fetchAllPullRequests(Long userId, String login, String token) {
 *     Instant now = Instant.now();
 *     return PaginationHelper.paginate(
 *         cursor -> fetchPullRequestsPage(login, cursor, token),
 *         UserPullRequestsResponse::getPageInfo,
 *         (data, _) -> savePullRequests(userId, data.getPullRequests(), now),
 *         "user",
 *         login,
 *         UserPullRequestsResponse.class
 *     );
 * }
 * }</pre>
 */
@Slf4j
public final class PaginationHelper {

    private PaginationHelper() {
        throw new AssertionError("Utility class");
    }

    /**
     * Paginates through GraphQL responses using cursor-based pagination.
     *
     * @param <T> the data type contained in the GraphQL response
     * @param fetcher function to fetch a page of data
     * @param pageInfoExtractor function to extract PageInfo from data
     * @param dataProcessor function to process data and return saved count
     * @param entityType the type of entity being queried
     * @param entityId the identifier of the entity
     * @param dataClass the Class object for the data type T
     * @return total count of items saved across all pages
     */
    public static <T> int paginate(
            Function<String, GraphQLResponse<T>> fetcher,
            Function<T, Object> pageInfoExtractor,
            BiFunction<T, String, Integer> dataProcessor,
            String entityType,
            String entityId,
            Class<T> dataClass
    ) {
        int totalSaved = 0;
        String cursor = null;
        do {
            PageResult<T> result = fetchAndProcessPage(fetcher, pageInfoExtractor, dataProcessor, entityType, entityId, cursor, dataClass);
            if (result.hasError) break;
            totalSaved += result.saved;
            cursor = result.nextCursor;
        } while (cursor != null);
        return totalSaved;
    }

    /**
     * Fetches and processes a single page of GraphQL results.
     *
     * @param <T> the data type
     * @param fetcher function to fetch a page
     * @param pageInfoExtractor function to extract PageInfo
     * @param dataProcessor function to process data
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param cursor the current cursor
     * @param dataClass the Class object for type T
     * @return page result with saved count and next cursor
     */
    private static <T> PageResult<T> fetchAndProcessPage(
            Function<String, GraphQLResponse<T>> fetcher,
            Function<T, Object> pageInfoExtractor,
            BiFunction<T, String, Integer> dataProcessor,
            String entityType,
            String entityId,
            String cursor,
            Class<T> dataClass
    ) {
        GraphQLResponse<T> response;
        try {
            response = fetcher.apply(cursor);
        } catch (Exception exception) {
            log.warn("HTTP error fetching page for {} {}: {}", entityType, entityId, exception.getMessage());
            return new PageResult<>(0, null, true);
        }
        return processResponse(response, pageInfoExtractor, dataProcessor, entityType, entityId, cursor, dataClass);
    }

    /**
     * Processes a GraphQL response after successful fetch.
     *
     * @param <T> the data type
     * @param response the GraphQL response
     * @param pageInfoExtractor function to extract PageInfo
     * @param dataProcessor function to process data
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param cursor the current cursor
     * @param dataClass the Class object for type T
     * @return page result with saved count and next cursor
     */
    private static <T> PageResult<T> processResponse(
            GraphQLResponse<T> response,
            Function<T, Object> pageInfoExtractor,
            BiFunction<T, String, Integer> dataProcessor,
            String entityType,
            String entityId,
            String cursor,
            Class<T> dataClass
    ) {
        if (GraphQLErrorHandler.logAndCheckErrors(response, entityType, entityId)) {
            return new PageResult<>(0, null, true);
        }
        T data = response.getDataAs(dataClass);
        int saved = dataProcessor.apply(data, cursor);
        Object pageInfo = pageInfoExtractor.apply(data);
        String nextCursor = extractCursor(pageInfo);
        return new PageResult<>(saved, nextCursor, false);
    }

    /**
     * Extracts cursor from PageInfo object.
     *
     * @param pageInfo the pagination metadata
     * @return the next cursor, or null if no more pages
     */
    private static String extractCursor(Object pageInfo) {
        if (pageInfo == null) {
            return null;
        }
        try {
            boolean hasNextPage = (boolean) pageInfo.getClass().getMethod("isHasNextPage").invoke(pageInfo);
            if (!hasNextPage) {
                return null;
            }
            return (String) pageInfo.getClass().getMethod("getEndCursor").invoke(pageInfo);
        } catch (Exception exception) {
            log.warn("Failed to extract cursor from PageInfo", exception);
            return null;
        }
    }

    /**
     * Result of processing a single page.
     *
     * @param <T> the data type
     */
    private static class PageResult<T> {
        final int saved;
        final String nextCursor;
        final boolean hasError;

        PageResult(int saved, String nextCursor, boolean hasError) {
            this.saved = saved;
            this.nextCursor = nextCursor;
            this.hasError = hasError;
        }
    }
}
