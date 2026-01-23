package io.swkoreatech.kosp.domain.auth.oauth2.service;

import static io.swkoreatech.kosp.global.constants.AuthConstants.*;

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

import org.springframework.security.crypto.encrypt.TextEncryptor;

import io.swkoreatech.kosp.domain.github.model.GithubUser;
import io.swkoreatech.kosp.domain.github.repository.GithubUserRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GithubUserRepository githubUserRepository;
    private final TextEncryptor textEncryptor;

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
            String encryptedToken = textEncryptor.encrypt(token);
            githubUser.updateProfile(
                oAuth2User.getAttribute("login"),
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("avatar_url"),
                encryptedToken
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
            // 탈퇴한 사용자는 재가입 가능하도록 새 사용자로 취급
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
        // 탈퇴한 사용자는 완전히 새 사용자로 취급 (USER_ATTR 제거)
        attributes.put(IS_REGISTERED_ATTR, false);
        attributes.put(NEEDS_ADDITIONAL_INFO_ATTR, true);
        return attributes;
    }

    private Map<String, Object> buildLoginAttributes(Map<String, Object> attributes, User user) {
        attributes.put(IS_REGISTERED_ATTR, true);
        attributes.put(USER_ATTR, user);
        return attributes;
    }

}
