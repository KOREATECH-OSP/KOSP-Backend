package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.repository;

import static kr.ac.koreatech.sw.kosp.global.constants.AuthConstants.REDIRECT_URI_SESSION_ATTR;

import java.net.URI;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedirectHttpSessionOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> delegate = new HttpSessionOAuth2AuthorizationRequestRepository();
    private final CorsConfigurationSource corsConfigurationSource;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return delegate.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        delegate.saveAuthorizationRequest(authorizationRequest, request, response);
        saveRedirectUri(authorizationRequest, request);
    }

    private void saveRedirectUri(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
        if (authorizationRequest == null) {
            return;
        }
        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null && isAuthorizedRedirectUri(redirectUri, request)) {
            saveToSession(request, redirectUri);
        }
    }

    private void saveToSession(HttpServletRequest request, String redirectUri) {
        request.getSession().setAttribute(REDIRECT_URI_SESSION_ATTR, redirectUri);
    }

    private boolean isAuthorizedRedirectUri(String uri, HttpServletRequest request) {
        CorsConfiguration configuration = corsConfigurationSource.getCorsConfiguration(request);
        if (configuration == null) {
            return false;
        }

        try {
            URI clientRedirectUri = URI.create(uri);
            String origin = clientRedirectUri.getScheme() + "://" + clientRedirectUri.getAuthority();
            return configuration.checkOrigin(origin) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return delegate.removeAuthorizationRequest(request, response);
    }
}
