package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.service;

import static kr.ac.koreatech.sw.kosp.global.constants.AuthConstants.*;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.ac.koreatech.sw.kosp.domain.auth.oauth2.dto.response.OAuth2Response;
import kr.ac.koreatech.sw.kosp.domain.auth.service.AuthService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService {

    private final AuthService authService;

    public String oAuth2ResultHandler(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = determineTargetUrl(request);
        return processOAuth2Result(targetUrl, request, response);
    }

    private String processOAuth2Result(String targetUrl, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            boolean isRegistered = Boolean.TRUE.equals(attributes.get(IS_REGISTERED_ATTR));

            if (isRegistered) {
                User user = (User)attributes.get(USER_ATTR);
                authService.login(user.getKutEmail(), user.getPassword(), request, response);
            }
        }

        return addQueryParams(targetUrl, request);
    }

    private String determineTargetUrl(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "/";
        }
        return getRedirectUriFromSession(session);
    }

    private String getRedirectUriFromSession(HttpSession session) {
        String redirectUri = (String)session.getAttribute(REDIRECT_URI_SESSION_ATTR);
        if (redirectUri != null && !redirectUri.isBlank()) {
            session.removeAttribute(REDIRECT_URI_SESSION_ATTR);
            return redirectUri;
        }
        return "/";
    }

    private String addQueryParams(String targetUrl, HttpServletRequest request) {
        boolean isNew = Boolean.TRUE.equals(request.getAttribute("isNew"));
        Object id = request.getAttribute("githubId");
        Long githubId = (id instanceof Number number) ? number.longValue() : Long.parseLong(String.valueOf(id));

        return OAuth2Response.of(isNew, githubId).appendQueryParams(targetUrl);
    }
}
