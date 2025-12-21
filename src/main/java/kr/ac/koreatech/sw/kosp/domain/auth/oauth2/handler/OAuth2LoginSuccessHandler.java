package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.handler;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GithubUserRepository githubUserRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
        Long githubId = getGithubId(oAuth2User);

        Optional<GithubUser> githubUserOptional = githubUserRepository.findByGithubId(githubId);
        boolean isNew = githubUserOptional.isEmpty();

        GithubUser githubUser = getOrUpdateUser(githubUserOptional, oAuth2User, authentication);
        githubUserRepository.save(githubUser);

        setRequestAttributes(request, isNew, githubId);
        request.getRequestDispatcher("/v1/oauth2/result").forward(request, response);
    }

    private Long getGithubId(OAuth2User oAuth2User) {
        return Optional.ofNullable(oAuth2User.getAttribute("id"))
            .map(Object::toString)
            .map(Long::valueOf)
            .orElseThrow(() -> new IllegalArgumentException("Github ID not found"));
    }

    private GithubUser getOrUpdateUser(
        Optional<GithubUser> optional,
        OAuth2User oAuth2User,
        Authentication authentication
    ) {
        return optional.map(githubUser -> updateExistingUser(githubUser, oAuth2User, authentication))
            .orElseGet(() -> createNewUser(oAuth2User, authentication));
    }

    private GithubUser updateExistingUser(GithubUser user, OAuth2User oAuth2User, Authentication authentication) {
        String accessToken = getAccessToken(authentication);
        user.updateProfile(
            oAuth2User.getAttribute("login"),
            oAuth2User.getAttribute("name"),
            oAuth2User.getAttribute("avatar_url"),
            accessToken
        );
        return user;
    }

    private GithubUser createNewUser(OAuth2User oAuth2User, Authentication authentication) {
        String accessToken = getAccessToken(authentication);
        return GithubUser.builder()
            .githubId(getGithubId(oAuth2User))
            .githubLogin(oAuth2User.getAttribute("login"))
            .githubName(oAuth2User.getAttribute("name"))
            .githubAvatarUrl(oAuth2User.getAttribute("avatar_url"))
            .githubToken(accessToken)
            .build();
    }

    private String getAccessToken(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(),
            oauthToken.getName()
        );
        return authorizedClient.getAccessToken().getTokenValue();
    }

    private void setRequestAttributes(HttpServletRequest request, boolean isNew, Long githubId) {
        request.setAttribute("isNew", isNew);
        request.setAttribute("githubId", githubId);
    }
}
