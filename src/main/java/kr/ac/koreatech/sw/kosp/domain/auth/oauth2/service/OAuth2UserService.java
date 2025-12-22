package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.service;

import static kr.ac.koreatech.sw.kosp.global.constants.AuthConstants.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.ac.koreatech.sw.kosp.domain.auth.oauth2.dto.response.OAuth2Response;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GithubUserRepository githubUserRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long githubId = getGithubId(attributes);
        String githubAccessToken = userRequest.getAccessToken().getTokenValue();

        Optional<User> userOptional = userRepository.findByGithubUser_GithubId(githubId);
        updateOrSaveGithubUser(userOptional, oAuth2User, githubAccessToken, githubId);

        Map<String, Object> modifiedAttributes = buildAttributes(attributes, userOptional);

        return new DefaultOAuth2User(Collections.emptyList(), modifiedAttributes, "id");
    }

    private Long getGithubId(Map<String, Object> attributes) {
        Object id = attributes.get("id");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(id));
    }

    private void updateOrSaveGithubUser(
            Optional<User> userOptional,
            OAuth2User oAuth2User,
            String token,
            Long githubId) {
        GithubUser githubUser = userOptional.map(User::getGithubUser)
                .orElseGet(() -> githubUserRepository.findByGithubId(githubId)
                        .orElseGet(() -> GithubUser.builder().githubId(githubId).build()));

        saveGithubUser(githubUser, oAuth2User, token);
    }

    private void saveGithubUser(GithubUser githubUser, OAuth2User oAuth2User, String token) {
        githubUser.updateProfile(
                oAuth2User.getAttribute("login"),
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("avatar_url"),
                token);
        githubUserRepository.save(githubUser);
    }

    private Map<String, Object> buildAttributes(Map<String, Object> original, Optional<User> userOptional) {
        Map<String, Object> attributes = new LinkedHashMap<>(original);
        if (userOptional.isEmpty()) {
            return buildNewUserAttributes(attributes);
        }
        return buildExistingUserAttributes(attributes, userOptional.get());
    }

    private Map<String, Object> buildExistingUserAttributes(Map<String, Object> attributes, User user) {
        if (user.isDeleted()) {
            return buildReregistrationAttributes(attributes, user);
        }
        return buildLoginAttributes(attributes, user);
    }

    private Map<String, Object> buildNewUserAttributes(Map<String, Object> attributes) {
        attributes.put(IS_REGISTERED_ATTR, false);
        attributes.put(NEEDS_ADDITIONAL_INFO_ATTR, true);
        return attributes;
    }

    private Map<String, Object> buildReregistrationAttributes(Map<String, Object> attributes, User user) {
        attributes.put(IS_REGISTERED_ATTR, false);
        attributes.put(IS_REREGISTRATION_ATTR, true);
        attributes.put(USER_ATTR, user);
        return attributes;
    }

    private Map<String, Object> buildLoginAttributes(Map<String, Object> attributes, User user) {
        attributes.put(IS_REGISTERED_ATTR, true);
        attributes.put(USER_ATTR, user);
        return attributes;
    }

    public String oAuth2ResultHandler(HttpServletRequest request) {
        String targetUrl = determineTargetUrl(request);
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
        String redirectUri = (String) session.getAttribute(REDIRECT_URI_SESSION_ATTR);
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
