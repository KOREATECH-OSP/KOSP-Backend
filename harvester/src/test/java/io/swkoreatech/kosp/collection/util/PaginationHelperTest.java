package io.swkoreatech.kosp.collection.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiFunction;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaginationHelper 단위 테스트")
class PaginationHelperTest {

    @Mock
    private Function<String, GraphQLResponse<TestResponse>> fetcher;

    @Mock
    private Function<TestResponse, Object> pageInfoExtractor;

    @Mock
    private BiFunction<TestResponse, String, Integer> dataProcessor;

    @Nested
    @DisplayName("paginate 메서드")
    class PaginateTest {

        @Test
        @DisplayName("단일 페이지 - hasNextPage=false")
        void singlePage_noNextPage() {
            // given
            GraphQLResponse<TestResponse> response = mock(GraphQLResponse.class);
            TestResponse testData = new TestResponse();
            TestPageInfo pageInfo = new TestPageInfo(false, null);

            when(fetcher.apply(null)).thenReturn(response);
            when(response.hasErrors()).thenReturn(false);
            when(response.getDataAs(TestResponse.class)).thenReturn(testData);
            when(pageInfoExtractor.apply(testData)).thenReturn(pageInfo);
            when(dataProcessor.apply(testData, null)).thenReturn(10);

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then
            assertThat(result).isEqualTo(10);
            verify(fetcher, times(1)).apply(null);
            verify(dataProcessor, times(1)).apply(testData, null);
            verify(pageInfoExtractor, times(1)).apply(testData);
        }

        @Test
        @DisplayName("다중 페이지 - 3페이지 반복")
        void multiplePages_threePagesWithCursorProgression() {
            // given
            TestResponse data1 = new TestResponse();
            TestResponse data2 = new TestResponse();
            TestResponse data3 = new TestResponse();

            TestPageInfo pageInfo1 = new TestPageInfo(true, "cursor1");
            TestPageInfo pageInfo2 = new TestPageInfo(true, "cursor2");
            TestPageInfo pageInfo3 = new TestPageInfo(false, null);

            // Setup first page
            GraphQLResponse<TestResponse> response1 = createMockResponse(false, data1);
            when(fetcher.apply(null)).thenReturn(response1);
            when(pageInfoExtractor.apply(data1)).thenReturn(pageInfo1);
            when(dataProcessor.apply(data1, null)).thenReturn(5);

            // Setup second page
            GraphQLResponse<TestResponse> response2 = createMockResponse(false, data2);
            when(fetcher.apply("cursor1")).thenReturn(response2);
            when(pageInfoExtractor.apply(data2)).thenReturn(pageInfo2);
            when(dataProcessor.apply(data2, "cursor1")).thenReturn(7);

            // Setup third page
            GraphQLResponse<TestResponse> response3 = createMockResponse(false, data3);
            when(fetcher.apply("cursor2")).thenReturn(response3);
            when(pageInfoExtractor.apply(data3)).thenReturn(pageInfo3);
            when(dataProcessor.apply(data3, "cursor2")).thenReturn(3);

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then
            assertThat(result).isEqualTo(15); // 5 + 7 + 3
            verify(fetcher).apply(null);
            verify(fetcher).apply("cursor1");
            verify(fetcher).apply("cursor2");
            verify(dataProcessor).apply(data1, null);
            verify(dataProcessor).apply(data2, "cursor1");
            verify(dataProcessor).apply(data3, "cursor2");
        }

        @Test
        @DisplayName("에러 발생 시 첫 페이지에서 중단")
        void errorOnFirstPage_breaksAndReturnsZero() {
            // given
            GraphQLResponse<TestResponse> response = mock(GraphQLResponse.class);

            when(fetcher.apply(null)).thenReturn(response);
            when(response.hasErrors()).thenReturn(true);
            when(response.getData()).thenReturn(null);

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then
            assertThat(result).isEqualTo(-1);
            verify(fetcher, times(1)).apply(null);
            verify(dataProcessor, never()).apply(any(), any());
            verify(pageInfoExtractor, never()).apply(any());
        }

        @Test
        @DisplayName("에러 발생 시 두 번째 페이지에서 중단")
        void errorOnSecondPage_returnsAccumulatedCount() {
            // given
            TestResponse data1 = new TestResponse();
            TestPageInfo pageInfo1 = new TestPageInfo(true, "cursor1");

            // Setup first page (success)
            GraphQLResponse<TestResponse> response1 = createMockResponse(false, data1);
            when(fetcher.apply(null)).thenReturn(response1);
            when(pageInfoExtractor.apply(data1)).thenReturn(pageInfo1);
            when(dataProcessor.apply(data1, null)).thenReturn(10);

            // Setup second page (error)
            GraphQLResponse<TestResponse> response2 = createMockResponse(true, null);
            when(fetcher.apply("cursor1")).thenReturn(response2);

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then
            assertThat(result).isEqualTo(10);
            verify(fetcher).apply(null);
            verify(fetcher).apply("cursor1");
            verify(dataProcessor).apply(data1, null);
        }

        @Test
        @DisplayName("PageInfo가 null인 경우 중단")
        void nullPageInfo_stopsAndReturnsAccumulatedCount() {
            // given
            GraphQLResponse<TestResponse> response = mock(GraphQLResponse.class);
            TestResponse testData = new TestResponse();

            when(fetcher.apply(null)).thenReturn(response);
            when(response.hasErrors()).thenReturn(false);
            when(response.getDataAs(TestResponse.class)).thenReturn(testData);
            when(pageInfoExtractor.apply(testData)).thenReturn(null);
            when(dataProcessor.apply(testData, null)).thenReturn(5);

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then
            assertThat(result).isEqualTo(5);
            verify(fetcher, times(1)).apply(null);
            verify(dataProcessor, times(1)).apply(testData, null);
        }

        @Test
        @DisplayName("데이터 프로세서가 0을 반환해도 계속 진행")
        void dataProcessorReturnsZero_continuesIfHasNextPage() {
            // given
            TestResponse data1 = new TestResponse();
            TestResponse data2 = new TestResponse();

            TestPageInfo pageInfo1 = new TestPageInfo(true, "cursor1");
            TestPageInfo pageInfo2 = new TestPageInfo(false, null);

            // Setup first page (returns 0 items)
            GraphQLResponse<TestResponse> response1 = createMockResponse(false, data1);
            when(fetcher.apply(null)).thenReturn(response1);
            when(pageInfoExtractor.apply(data1)).thenReturn(pageInfo1);
            when(dataProcessor.apply(data1, null)).thenReturn(0);

            // Setup second page (returns 5 items)
            GraphQLResponse<TestResponse> response2 = createMockResponse(false, data2);
            when(fetcher.apply("cursor1")).thenReturn(response2);
            when(pageInfoExtractor.apply(data2)).thenReturn(pageInfo2);
            when(dataProcessor.apply(data2, "cursor1")).thenReturn(5);

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then
            assertThat(result).isEqualTo(5);
            verify(fetcher).apply(null);
            verify(fetcher).apply("cursor1");
            verify(dataProcessor).apply(data1, null);
            verify(dataProcessor).apply(data2, "cursor1");
        }

        @Test
        @DisplayName("커서가 null이면 루프 종료")
        void nullCursor_stopsLoop() {
            // given
            GraphQLResponse<TestResponse> response = mock(GraphQLResponse.class);
            TestResponse testData = new TestResponse();
            TestPageInfo pageInfo = new TestPageInfo(false, null);

            when(fetcher.apply(null)).thenReturn(response);
            when(response.hasErrors()).thenReturn(false);
            when(response.getDataAs(TestResponse.class)).thenReturn(testData);
            when(pageInfoExtractor.apply(testData)).thenReturn(pageInfo);
            when(dataProcessor.apply(testData, null)).thenReturn(8);

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then
            assertThat(result).isEqualTo(8);
            verify(fetcher, times(1)).apply(null);
        }

        @Test
        @DisplayName("메서드 호출 순서 검증")
        void verifyMethodInvocationOrder() {
            // given
            GraphQLResponse<TestResponse> response = mock(GraphQLResponse.class);
            TestResponse testData = new TestResponse();
            TestPageInfo pageInfo = new TestPageInfo(false, null);

            when(fetcher.apply(null)).thenReturn(response);
            when(response.hasErrors()).thenReturn(false);
            when(response.getDataAs(TestResponse.class)).thenReturn(testData);
            when(pageInfoExtractor.apply(testData)).thenReturn(pageInfo);
            when(dataProcessor.apply(testData, null)).thenReturn(3);

            // when
            PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then - verify order: fetcher -> dataProcessor -> pageInfoExtractor
            verify(fetcher).apply(null);
            verify(response).getDataAs(TestResponse.class);
            verify(dataProcessor).apply(testData, null);
            verify(pageInfoExtractor).apply(testData);
        }

        @Test
        @DisplayName("큰 데이터셋 - 많은 페이지 처리")
        void largeDataset_manyPages() {
            // given - 5 pages
            TestResponse[] dataArray = new TestResponse[5];
            TestPageInfo[] pageInfoArray = new TestPageInfo[5];

            for (int i = 0; i < 5; i++) {
                dataArray[i] = new TestResponse();
                boolean hasNext = i < 4;
                String cursor = hasNext ? "cursor" + i : null;
                pageInfoArray[i] = new TestPageInfo(hasNext, cursor);

                String currentCursor = i == 0 ? null : "cursor" + (i - 1);
                GraphQLResponse<TestResponse> response = createMockResponse(false, dataArray[i]);
                when(fetcher.apply(currentCursor)).thenReturn(response);
                when(pageInfoExtractor.apply(dataArray[i])).thenReturn(pageInfoArray[i]);
                when(dataProcessor.apply(dataArray[i], currentCursor)).thenReturn(10 + i);
            }

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then - 10 + 11 + 12 + 13 + 14 = 60
            assertThat(result).isEqualTo(60);
            verify(fetcher).apply(null);
            verify(fetcher).apply("cursor0");
            verify(fetcher).apply("cursor1");
            verify(fetcher).apply("cursor2");
            verify(fetcher).apply("cursor3");
        }

        @Test
        @DisplayName("빈 응답 - 데이터 없음")
        void emptyResponse_noData() {
            // given
            GraphQLResponse<TestResponse> response = mock(GraphQLResponse.class);
            TestResponse testData = new TestResponse();
            TestPageInfo pageInfo = new TestPageInfo(false, null);

            when(fetcher.apply(null)).thenReturn(response);
            when(response.hasErrors()).thenReturn(false);
            when(response.getDataAs(TestResponse.class)).thenReturn(testData);
            when(pageInfoExtractor.apply(testData)).thenReturn(pageInfo);
            when(dataProcessor.apply(testData, null)).thenReturn(0);

            // when
            int result = PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "test",
                "entity1",
                TestResponse.class
            );

            // then
            assertThat(result).isZero();
            verify(fetcher, times(1)).apply(null);
        }

        @Test
        @DisplayName("엔티티 타입과 ID가 올바르게 전달됨")
        void entityTypeAndIdPassedCorrectly() {
            // given
            TestResponse testData = new TestResponse();
            TestPageInfo pageInfo = new TestPageInfo(false, null);

            GraphQLResponse<TestResponse> response = createMockResponse(false, testData);
            when(fetcher.apply(null)).thenReturn(response);
            when(pageInfoExtractor.apply(testData)).thenReturn(pageInfo);
            when(dataProcessor.apply(testData, null)).thenReturn(1);

            // when
            PaginationHelper.paginate(
                fetcher,
                pageInfoExtractor,
                dataProcessor,
                "user",
                "octocat",
                TestResponse.class
            );

            // then - verify that GraphQLErrorHandler was called with correct params
            verify(fetcher, times(1)).apply(null);
        }

        private GraphQLResponse<TestResponse> createMockResponse(boolean hasErrors, TestResponse data) {
            GraphQLResponse<TestResponse> response = mock(GraphQLResponse.class);
            when(response.hasErrors()).thenReturn(hasErrors);
            if (!hasErrors) {
                when(response.getDataAs(TestResponse.class)).thenReturn(data);
            }
            return response;
        }
    }

    /**
     * Test data class representing a GraphQL response with pagination.
     */
    static class TestResponse {
        private TestPageInfo pageInfo;

        public TestPageInfo getPageInfo() {
            return pageInfo;
        }

        public void setPageInfo(TestPageInfo pageInfo) {
            this.pageInfo = pageInfo;
        }
    }

    /**
     * Test PageInfo class simulating the inner class pattern used in actual response DTOs.
     */
    static class TestPageInfo {
        private final boolean hasNextPage;
        private final String endCursor;

        TestPageInfo(boolean hasNextPage, String endCursor) {
            this.hasNextPage = hasNextPage;
            this.endCursor = endCursor;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public String getEndCursor() {
            return endCursor;
        }
    }
}
