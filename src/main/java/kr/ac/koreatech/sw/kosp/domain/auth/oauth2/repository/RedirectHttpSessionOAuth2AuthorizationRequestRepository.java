package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.repository;

import static kr.ac.koreatech.sw.kosp.global.constants.AuthConstants.REDIRECT_URI_SESSION_ATTR;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RedirectHttpSessionOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> delegate = new HttpSessionOAuth2AuthorizationRequestRepository();

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
        saveToSession(request, redirectUri);
    }

    private void saveToSession(HttpServletRequest request, String redirectUri) {
        if (isInvalid(redirectUri)) {
            return;
        }
        request.getSession().setAttribute(REDIRECT_URI_SESSION_ATTR, redirectUri);
    }

    private boolean isInvalid(String redirectUri) {
        return redirectUri == null || redirectUri.isBlank();
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        return delegate.removeAuthorizationRequest(request, response);
    }
}
