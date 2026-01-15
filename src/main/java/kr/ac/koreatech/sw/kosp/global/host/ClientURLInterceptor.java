package kr.ac.koreatech.sw.kosp.global.host;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClientURLInterceptor implements HandlerInterceptor {

    private final ClientURLContext clientURLContext;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
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

        return (serverPort != 80 && serverPort != 443) ?
            String.format("%s://%s:%d", scheme, serverName, serverPort) :
            String.format("%s://%s", scheme, serverName);
    }
}
