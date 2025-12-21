package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        setRequestAttributes(request, oAuth2User);

        request.getRequestDispatcher("/v1/oauth2/result").forward(request, response);
    }

    private void setRequestAttributes(HttpServletRequest request, OAuth2User oAuth2User) {
        boolean isRegistered = Boolean.TRUE.equals(oAuth2User.getAttribute("isRegistered"));
        request.setAttribute("isNew", !isRegistered);
        request.setAttribute("githubId", oAuth2User.getAttribute("id"));
    }
}
