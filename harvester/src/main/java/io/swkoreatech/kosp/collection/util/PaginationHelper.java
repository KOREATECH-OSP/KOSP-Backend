package io.swkoreatech.kosp.collection.util;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;

import java.util.function.BiFunction;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

/**
 * 제네릭 데이터 타입을 지원하는 GraphQL 페이지네이션 처리 유틸리티 클래스.
 *
 * <p>여러 마이닝 스텝(PullRequestMiningStep, IssueMiningStep, CommitMiningStep)에서
 * 공통으로 사용되는 페이지네이션 패턴을 추상화한다. 커서 기반 페이지네이션을
 * do-while 루프로 관리하며, GraphQLErrorHandler와 통합하여 에러를 처리한다.
 *
 * <p><b>제네릭 타입 파라미터:</b>
 * <ul>
 *   <li>{@code T} - GraphQL 응답에서 추출된 데이터 클래스 타입
 *       (예: UserPullRequestsResponse, UserIssuesResponse, RepositoryCommitsResponse).
 *       페이지네이션 메타데이터를 위한 PageInfo 객체를 포함해야 한다.</li>
 * </ul>
 *
 * <p><b>사용 예시:</b>
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
     * 커서 기반 페이지네이션으로 GraphQL 응답을 순회한다.
     *
     * @param <T>               GraphQL 응답에 포함된 데이터 타입
     * @param fetcher           단일 페이지 데이터를 가져오는 함수
     * @param pageInfoExtractor 데이터에서 PageInfo를 추출하는 함수
     * @param dataProcessor     데이터를 처리하고 저장 건수를 반환하는 함수
     * @param entityType        조회 대상 엔티티 타입 (예: "user", "repo")
     * @param entityId          엔티티 식별자 (예: 로그인 이름, "owner/name")
     * @param dataClass         데이터 타입 T의 Class 객체
     * @return 전체 페이지에서 저장된 항목의 총 수
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
            PageResult<T> result = fetchAndProcessPage(
                    fetcher, pageInfoExtractor, dataProcessor, entityType, entityId, cursor, dataClass
            );
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
        GraphQLResponse<T> response = fetcher.apply(cursor);
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
