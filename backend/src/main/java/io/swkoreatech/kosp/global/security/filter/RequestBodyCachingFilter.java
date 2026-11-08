package io.swkoreatech.kosp.global.security.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 요청 바디를 미리 읽어 캐싱하는 필터.
 * <p>POST 요청의 InputStream은 한 번만 읽을 수 있는데, 앞단 필터가 소비하면
 * {@code @RequestBody}가 빈 스트림을 받는 문제를 방지한다.
 * 필터 체인 최우선 순위로 실행되어 바디를 바이트 배열에 캐시하고,
 * 이후 {@code getInputStream()}은 항상 캐시에서 새 스트림을 반환한다.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestBodyCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String contentType = request.getContentType();
        boolean isJsonPost = "POST".equalsIgnoreCase(request.getMethod())
            && contentType != null
            && contentType.contains("application/json");

        if (isJsonPost) {
            byte[] body = request.getInputStream().readAllBytes();
            log.debug("[RequestBodyCachingFilter] URI={} bodyLength={} body={}",
                request.getRequestURI(), body.length, new String(body));
            filterChain.doFilter(new CachedBodyRequestWrapper(request, body), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        CachedBodyRequestWrapper(HttpServletRequest request, byte[] cachedBody) {
            super(request);
            this.cachedBody = cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override public int read() { return byteArrayInputStream.read(); }
                @Override public boolean isFinished() { return byteArrayInputStream.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) {}
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }
    }
}
