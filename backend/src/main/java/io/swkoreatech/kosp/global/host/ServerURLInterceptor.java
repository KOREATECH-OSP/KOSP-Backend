package io.swkoreatech.kosp.global.host;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerURLInterceptor implements HandlerInterceptor {

    private final ServerURLContext serverURLContext;

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
