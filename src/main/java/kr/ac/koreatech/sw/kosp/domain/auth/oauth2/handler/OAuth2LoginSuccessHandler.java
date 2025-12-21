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

        GithubUser githubUser = getOrUpdateUser(githubUserOptional, oAuth2User, authentication);
        githubUserRepository.save(githubUser);

        setRequestAttributes(request, isNew, githubId);
        request.getRequestDispatcher("/v1/oauth2/result").forward(request, response);
    }

    private void setRequestAttributes(HttpServletRequest request, boolean isNew, Long githubId) {
        request.setAttribute("isNew", isNew);
        request.setAttribute("githubId", githubId);
    }
}
