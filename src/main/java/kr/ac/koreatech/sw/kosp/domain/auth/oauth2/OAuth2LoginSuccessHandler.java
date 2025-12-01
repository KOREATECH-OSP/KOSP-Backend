package kr.ac.koreatech.sw.kosp.domain.auth.oauth2;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
        log.info("oAuth2User: {}", oAuth2User);
        log.info("oauthToken: {}", oauthToken);

        Long githubId = Long.valueOf(oAuth2User.getAttribute("id").toString());
        String login = oAuth2User.getAttribute("login");
        String name = oAuth2User.getAttribute("name");

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(),
            oauthToken.getName()
        );
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        String avatarUrl = oAuth2User.getAttribute("avatar_url");

        Optional<GithubUser> githubUserOptional = githubUserRepository.findByGithubId(githubId);

        request.setAttribute("isNew", githubUserOptional.isPresent());
        request.setAttribute("githubId", githubId);

        GithubUser githubUser;
        if (githubUserOptional.isPresent()) {
            /* 로그인 */
            githubUser = githubUserOptional.get();

            githubUser.updateProfile(login, name, avatarUrl, accessToken);
            githubUserRepository.save(githubUser);
        } else {
            /* 회원가입 */
            githubUser = GithubUser.builder()
                .githubId(githubId)
                .githubLogin(login)
                .githubName(name)
                .githubAvatarUrl(avatarUrl)
                .githubToken(accessToken)
                .build();

            githubUserRepository.save(githubUser);
        }

        request.getRequestDispatcher("/v1/auth/oauth2/result").forward(request, response);
    }
}
