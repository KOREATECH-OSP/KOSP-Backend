package io.swkoreatech.kosp.global.host;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 서버 URL을 추출하여 {@link ServerURLContext}에 설정하는 인터셉터.
 * <p>요청의 스킴, 호스트명, 포트를 조합하여 서버 URL을 생성한다.</p>
 */
@Component
@RequiredArgsConstructor
public class ServerURLInterceptor implements HandlerInterceptor {

    private final ServerURLContext serverURLContext;

    /** {@inheritDoc} */
    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) {
        String serverURL = getServerURL(request);
        serverURLContext.setServerURL(serverURL);
        return true;
    }

    /**
     * HTTP 요청으로부터 서버 URL을 생성한다.
     *
     * @param request HTTP 요청 객체
     * @return 서버 URL (예: {@code https://example.com} 또는 {@code http://localhost:8080})
     */
    public String getServerURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        if (serverPort != 80 && serverPort != 443) {
            return String.format("%s://%s:%d", scheme, serverName, serverPort);
        }
        return String.format("%s://%s", scheme, serverName);
    }
}
