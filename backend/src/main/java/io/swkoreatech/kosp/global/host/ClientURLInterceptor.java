package io.swkoreatech.kosp.global.host;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 클라이언트 URL을 추출하여 {@link ClientURLContext}에 설정하는 인터셉터.
 * <p>요청 헤더의 Origin 또는 Referer로부터 클라이언트 URL을 추출하며,
 * 둘 다 없는 경우 서버 URL을 대체값으로 사용한다.</p>
 */
@Component
@RequiredArgsConstructor
public class ClientURLInterceptor implements HandlerInterceptor {

    private final ClientURLContext clientURLContext;

    /** {@inheritDoc} */
    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) {
        String clientURL = getClientURL(request);
        clientURLContext.setClientURL(clientURL);
        return true;
    }

    private String getClientURL(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (StringUtils.hasText(origin)) {
            return origin;
        }

        String referer = request.getHeader("Referer");
        if (StringUtils.hasText(referer)) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                // Return scheme://authority (e.g., https://domain.com)
                return uri.getScheme() + "://" + uri.getAuthority();
            } catch (Exception e) {
                // Ignore parse errors
            }
        }

        return getServerURL(request);
    }

    private String getServerURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        if (serverPort != 80 && serverPort != 443) {
            return String.format("%s://%s:%d", scheme, serverName, serverPort);
        }
        return String.format("%s://%s", scheme, serverName);
    }
}
