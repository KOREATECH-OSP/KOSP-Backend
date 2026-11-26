package io.swkoreatech.kosp.global.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.global.common.IntegrationTestSupport;

/**
 * 오류 응답에 내부 정보(스택트레이스·예외 클래스·패키지명)가 새지 않는지 지키는 회귀 테스트.
 *
 * <p>운영에서 {@code NoHandlerFoundException} 스택트레이스가 Whitelabel 페이지로 노출된 사고(2026-11)의
 * 재발 방지 장치다. 실제 Spring Boot 컨텍스트(보안 필터·{@code add-mappings: false} 포함)로 검증한다.</p>
 */
@DisplayName("GlobalExceptionHandler - 오류 응답에 내부 정보를 노출하지 않는다")
@Import(GlobalExceptionHandlerTest.ThrowingController.class)
class GlobalExceptionHandlerTest extends IntegrationTestSupport {

    private static final String UNKNOWN_PATH = "/v1/definitely-not-a-route";

    /** 테스트 전용 컨트롤러. 일부러 예외를 던진다. */
    @RestController
    static class ThrowingController {

        @GetMapping("/test-only/errors/runtime")
        String runtime() {
            throw new IllegalStateException("internal detail: secret-table-name");
        }

        @GetMapping("/test-only/errors/global")
        String global() {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

    private MvcResult call(String path, MediaType accept) throws Exception {
        return mockMvc.perform(get(path).accept(accept)).andReturn();
    }

    private void assertNoInternals(String body) {
        assertThat(body).doesNotContain("org.springframework");
        assertThat(body).doesNotContain("Exception");
        assertThat(body).doesNotContain("trace");
        assertThat(body).doesNotContain("\tat ");
        assertThat(body).doesNotContain("io.swkoreatech");
    }

    @Nested
    @DisplayName("없는 경로")
    class UnknownPath {

        @Test
        @DisplayName("GET → 404 이고 응답 본문에 org.springframework 가 없다")
        void notFoundWithoutFrameworkNames() throws Exception {
            MvcResult result = call(UNKNOWN_PATH, MediaType.APPLICATION_JSON);

            assertThat(result.getResponse().getStatus()).isEqualTo(404);
            assertThat(result.getResponse().getContentAsString()).doesNotContain("org.springframework");
        }

        @Test
        @DisplayName("GET → 응답 본문에 Exception 이 없고 우리 형식(message, status) JSON 이다")
        void notFoundWithoutExceptionName() throws Exception {
            MvcResult result = call(UNKNOWN_PATH, MediaType.APPLICATION_JSON);
            String body = result.getResponse().getContentAsString();

            assertNoInternals(body);
            assertThat(body).isEqualTo("{\"message\":\"리소스를 찾을 수 없습니다.\",\"status\":404}");
        }

        @Test
        @DisplayName("Accept: text/html 이어도 Whitelabel 페이지가 아니라 JSON 이 온다")
        void htmlAcceptStillJson() throws Exception {
            MvcResult result = call(UNKNOWN_PATH, MediaType.TEXT_HTML);
            String body = result.getResponse().getContentAsString();

            assertThat(result.getResponse().getStatus()).isEqualTo(404);
            assertThat(result.getResponse().getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertThat(body).doesNotContain("Whitelabel").doesNotContain("<html");
            assertNoInternals(body);
        }
    }

    @Nested
    @DisplayName("처리되지 않은 예외")
    class UnexpectedException {

        @Test
        @DisplayName("RuntimeException → 500 이고 응답 본문에 스택트레이스·예외 메시지가 없다")
        void runtimeExceptionHidden() throws Exception {
            MvcResult result = call("/test-only/errors/runtime", MediaType.APPLICATION_JSON);
            String body = result.getResponse().getContentAsString();

            assertThat(result.getResponse().getStatus()).isEqualTo(500);
            assertNoInternals(body);
            assertThat(body).doesNotContain("secret-table-name");
            assertThat(body).isEqualTo("{\"message\":\"서버 내부 오류가 발생했습니다.\",\"status\":500}");
        }

        @Test
        @DisplayName("RuntimeException 을 Accept: text/html 로 호출해도 JSON 이 온다")
        void runtimeExceptionHtmlAccept() throws Exception {
            MvcResult result = call("/test-only/errors/runtime", MediaType.TEXT_HTML);

            assertThat(result.getResponse().getStatus()).isEqualTo(500);
            assertThat(result.getResponse().getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertNoInternals(result.getResponse().getContentAsString());
        }
    }

    @Nested
    @DisplayName("기존 처리기·표준 상태 코드 유지")
    class Precedence {

        @Test
        @DisplayName("GlobalException 은 포괄 처리기가 아니라 기존 처리기가 잡아 원래 상태·메시지를 유지한다")
        void globalExceptionKeepsOwnHandler() throws Exception {
            MvcResult result = call("/test-only/errors/global", MediaType.APPLICATION_JSON);

            assertThat(result.getResponse().getStatus()).isEqualTo(403);
            assertThat(result.getResponse().getContentAsString())
                .isEqualTo("{\"message\":\"권한이 없습니다.\",\"status\":403}");
        }

        @Test
        @DisplayName("지원하지 않는 메서드는 500 이 아니라 405 를 유지한다")
        void methodNotAllowedStays405() throws Exception {
            MvcResult result = mockMvc.perform(delete("/test-only/errors/global")).andReturn();

            assertThat(result.getResponse().getStatus()).isEqualTo(405);
            assertNoInternals(result.getResponse().getContentAsString());
        }
    }
}
