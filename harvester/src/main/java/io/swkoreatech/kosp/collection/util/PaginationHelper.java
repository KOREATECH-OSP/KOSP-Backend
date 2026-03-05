package io.swkoreatech.kosp.collection.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;
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
 * <p><b>반환 값:</b>
 * <ul>
 *   <li>전체 페이지에서 저장된 항목의 총 수. 첫 페이지에서 전체 에러(데이터 없음) 발생 시
 *       -1(재시도 가능) 또는 -2(재시도 불가)를 반환하여 호출자가 다른 파라미터로 재시도할 수 있다.
 *       페이지네이션 중간 에러 시 그때까지 저장된 항목 수를 반환한다.</li>
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
     * <p>첫 페이지에서 재시도 불가 에러 발생 시 -2를 반환한다. 첫 페이지에서
     * 재시도 가능 에러 또는 기타 전체 에러(데이터 없음) 발생 시 -1을 반환한다.
     * 호출자는 이를 이용하여 다른 파라미터로 재시도할 수 있다.
     *
     * @param <T>               GraphQL 응답에 포함된 데이터 타입
     * @param fetcher           단일 페이지 데이터를 가져오는 함수
     * @param pageInfoExtractor 데이터에서 PageInfo를 추출하는 함수
     * @param dataProcessor     데이터를 처리하고 저장 건수를 반환하는 함수
     * @param entityType        조회 대상 엔티티 타입 (예: "user", "repo")
     * @param entityId          엔티티 식별자 (예: 로그인 이름, "owner/name")
     * @param dataClass         데이터 타입 T의 Class 객체
     * @return 전체 페이지에서 저장된 항목의 총 수, 재시도 불가 에러 시 -2, 재시도 가능 에러 시 -1
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
            if (result.hasError) {
                if (totalSaved == 0) {
                    return determineErrorReturnValue(result.errorType, entityType, entityId);
                }
                break;
            }
            totalSaved += result.saved;
            cursor = result.nextCursor;
        } while (cursor != null);
        return totalSaved;
    }

    private static int determineErrorReturnValue(GraphQLErrorType errorType, String entityType, String entityId) {
        if (errorType == GraphQLErrorType.NON_RETRYABLE) {
            log.warn("Skipping {} {} — non-retryable GraphQL error (retry won't help)", entityType, entityId);
            return -2;
        }
        return -1;
    }

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

    private static <T> PageResult<T> processResponse(
        GraphQLResponse<T> response,
        Function<T, Object> pageInfoExtractor,
        BiFunction<T, String, Integer> dataProcessor,
        String entityType,
        String entityId,
        String cursor,
        Class<T> dataClass
    ) {
        GraphQLErrorType errorType = GraphQLErrorHandler.classifyErrors(response, entityType, entityId);
        if (errorType != null) {
            return new PageResult<>(0, null, true, errorType);
        }
        T data = response.getDataAs(dataClass);
        int saved = dataProcessor.apply(data, cursor);
        Object pageInfo = pageInfoExtractor.apply(data);
        String nextCursor = extractCursor(pageInfo);
        return new PageResult<>(saved, nextCursor, false);
    }

    private static String extractCursor(Object pageInfo) {
        if (pageInfo == null) {
            return null;
        }
        try {
            boolean hasNextPage = (boolean)pageInfo.getClass().getMethod("isHasNextPage").invoke(pageInfo);
            if (!hasNextPage) {
                return null;
            }
            return (String)pageInfo.getClass().getMethod("getEndCursor").invoke(pageInfo);
        } catch (Exception exception) {
            log.warn("Failed to extract cursor from PageInfo", exception);
            return null;
        }
    }

    /**
     * 단일 페이지 처리 결과.
     *
     * @param <T> 데이터 타입
     */
    private static class PageResult<T> {
        final int saved;
        final String nextCursor;
        final boolean hasError;
        final GraphQLErrorType errorType;

        PageResult(int saved, String nextCursor, boolean hasError, GraphQLErrorType errorType) {
            this.saved = saved;
            this.nextCursor = nextCursor;
            this.hasError = hasError;
            this.errorType = errorType;
        }

        PageResult(int saved, String nextCursor, boolean hasError) {
            this(saved, nextCursor, hasError, null);
        }
    }
}
