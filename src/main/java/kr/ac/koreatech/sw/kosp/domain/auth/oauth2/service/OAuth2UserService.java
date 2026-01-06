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

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
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
        Long githubId
    ) {
        // Only update existing GithubUser - new users will be handled by UserService.signup()
        Optional<GithubUser> existingGithubUser = userOptional.map(User::getGithubUser)
            .or(() -> githubUserRepository.findByGithubId(githubId));
        
        existingGithubUser.ifPresent(githubUser -> {
            githubUser.updateProfile(
                oAuth2User.getAttribute("login"),
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("avatar_url"),
                token
            );
            githubUserRepository.save(githubUser);
        });
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
            throw new OAuth2AuthenticationException(ExceptionMessage.AUTHENTICATION.getMessage());
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

}
