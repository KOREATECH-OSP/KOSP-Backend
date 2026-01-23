package io.swkoreatech.kosp.global.host;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerURLInterceptor implements HandlerInterceptor {

    private final ServerURLContext serverURLContext;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String serverURL = getServerURL(request);
        serverURLContext.setServerURL(serverURL);
        return true;
    }

    public String getServerURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        return (serverPort != 80 && serverPort != 443) ?
            String.format("%s://%s:%d", scheme, serverName, serverPort) :
            String.format("%s://%s", scheme, serverName);
    }
}
