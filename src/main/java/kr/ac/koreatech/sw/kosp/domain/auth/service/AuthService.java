package kr.ac.koreatech.sw.kosp.domain.auth.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthMeResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.oauth2.service.OAuth2UserService;
import kr.ac.koreatech.sw.kosp.domain.mail.model.EmailVerification;
import kr.ac.koreatech.sw.kosp.domain.mail.service.EmailVerificationService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.auth.core.AuthToken;
import kr.ac.koreatech.sw.kosp.global.auth.model.AuthTokenCategory;
import kr.ac.koreatech.sw.kosp.global.auth.provider.LoginTokenProvider;
import kr.ac.koreatech.sw.kosp.global.auth.provider.SignupTokenProvider;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final LoginTokenProvider loginTokenProvider;
    private final SignupTokenProvider signupTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final EmailVerificationService emailVerificationService;
    private final OAuth2UserService oAuth2UserService;
    private final UserRepository userRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final TextEncryptor textEncryptor;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void sendCertificationMail(String email, String signupToken) {
        emailVerificationService.sendCertificationMail(email, signupToken);
    }

    @Transactional
    public String verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationService.verifyCode(email, code);

        String signupToken = verification.getSignupToken();
        if (signupToken == null) {
            return null;
        }

        // Renew JWS with Email Verified Claim
        AuthToken<Claims> oldToken = signupTokenProvider.parseSignupToken(signupToken);
        if (!oldToken.validate()) {
            throw new GlobalException(ExceptionMessage.INVALID_TOKEN);
        }

        Claims oldClaims = oldToken.getData();
        String subject = oldClaims.getSubject();
        
        Map<String, Object> newClaims = new HashMap<>(oldClaims);
        newClaims.put("kutEmail", email);
        newClaims.put("emailVerified", true);

        AuthToken<Claims> newToken = signupTokenProvider.createSignupToken(subject, newClaims);
        return ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) newToken).getToken();
    }

    /**
     * 회원가입 토큰 검증
     * 프론트엔드에서 회원가입 폼 진입 전 토큰의 유효성을 확인
     */
    public void validateSignupToken(String token) {
        AuthToken<Claims> authToken = signupTokenProvider.parseSignupToken(token);
        if (!authToken.validate()) {
            throw new GlobalException(ExceptionMessage.INVALID_TOKEN);
        }

        Claims claims = authToken.getData();
        String category = claims.get("category", String.class);
        if (!kr.ac.koreatech.sw.kosp.global.auth.model.AuthTokenCategory.SIGNUP.getValue().equals(category)) {
            throw new GlobalException(ExceptionMessage.INVALID_TOKEN);
        }
    }

    /**
     * 로그인 토큰 검증
     * Access Token의 유효성을 확인
     */
    public void validateLoginToken(String token) {
        AuthToken<Claims> authToken = loginTokenProvider.convertAuthToken(token);
        if (!authToken.validate()) {
            throw new GlobalException(ExceptionMessage.INVALID_TOKEN);
        }

        Claims claims = authToken.getData();
        String category = claims.get("category", String.class);
        if (!kr.ac.koreatech.sw.kosp.global.auth.model.AuthTokenCategory.LOGIN.getValue().equals(category)) {
            throw new GlobalException(ExceptionMessage.INVALID_TOKEN);
        }
    }

    /**
     * GitHub Access Token으로 회원가입 토큰 발급
     */
    @Transactional
    public String exchangeGithubTokenForSignup(String githubAccessToken) {
        OAuth2UserRequest userRequest = createOAuth2UserRequest(githubAccessToken);
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        Long githubId = extractGithubId(attributes);

        // 이미 가입된 사용자인지 확인
        if (userRepository.findByGithubUser_GithubId(githubId).isPresent()) {
            throw new GlobalException(ExceptionMessage.GITHUB_USER_ALREADY_EXISTS);
        }

        // GitHub Access Token 암호화 (JWS에 저장)
        String encryptedToken = textEncryptor.encrypt(githubAccessToken);

        // JWS 발급
        Map<String, Object> claims = new HashMap<>(attributes);
        claims.put("encryptedGithubToken", encryptedToken);

        AuthToken<Claims> token = signupTokenProvider.createSignupToken(
            String.valueOf(githubId),
            claims
        );
        
        return ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) token).getToken();
    }

    /**
     * 일반 로그인 (이메일 + 비밀번호)
     */
    public AuthTokenResponse login(LoginRequest request) {
        Authentication authentication = authenticate(request.email(), request.password());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        return createTokenResponse(user);
    }

    /**
     * GitHub 로그인
     */
    @Transactional
    public AuthTokenResponse loginWithGithub(String githubAccessToken) {
        OAuth2UserRequest userRequest = createOAuth2UserRequest(githubAccessToken);
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
        Long githubId = extractGithubId(oAuth2User.getAttributes());

        User user = userRepository.findByGithubUser_GithubId(githubId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.GITHUB_USER_NOT_FOUND));

        return createTokenResponse(user);
    }

    /**
     * 사용자 정보 조회
     */
    public AuthMeResponse getUserInfo(User user) {
        String profileImage = (user.getGithubUser() != null) ? user.getGithubUser().getGithubAvatarUrl() : null;
        
        return new AuthMeResponse(
            user.getId(),
            user.getKutEmail(),
            user.getName(),
            profileImage,
            user.getIntroduction()
        );
    }

    /**
     * User Entity 기반으로 토큰 생성
     */
    public AuthTokenResponse createTokensForUser(User user) {
        return createTokenResponse(user);
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public AuthTokenResponse reissue(String refreshToken) {
        // 1. Validate Refresh Token
        AuthToken<Claims> authToken = loginTokenProvider.convertAuthToken(refreshToken);
        if (!authToken.validate()) {
            throw new GlobalException(ExceptionMessage.INVALID_TOKEN);
        }

        // 2. Validate Redis
        Claims claims = authToken.getData();
        String userId = claims.getSubject();
        String savedToken = redisTemplate.opsForValue().get("refresh:" + userId);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new GlobalException(ExceptionMessage.INVALID_TOKEN);
        }

        // 3. Issue New Access Token
        UserDetails userDetails = userDetailsService.loadUser(userId, AuthTokenCategory.LOGIN);
        User user = (User) userDetails;

        AuthToken<Claims> newAccessToken = loginTokenProvider.reissueAccessToken(user);
        String accessTokenString = ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) newAccessToken).getToken();

        return new AuthTokenResponse(accessTokenString, refreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        loginTokenProvider.revokeRefreshToken(userId);
    }

    private Authentication authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(token);
    }

    private AuthTokenResponse createTokenResponse(User user) {
        AuthToken<Claims> accessToken = loginTokenProvider.createAccessToken(user);
        AuthToken<Claims> refreshToken = loginTokenProvider.createRefreshToken(user);

        String accessTokenString = ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) accessToken).getToken();
        String refreshTokenString = ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) refreshToken).getToken();

        return new AuthTokenResponse(accessTokenString, refreshTokenString);
    }

    private Long extractGithubId(Map<String, Object> attributes) {
        Object id = attributes.get("id");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(id));
    }

    private OAuth2UserRequest createOAuth2UserRequest(String githubAccessToken) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("github");
        if (clientRegistration == null) {
            throw new GlobalException(ExceptionMessage.GITHUB_CLIENT_REGISTRATION_ERROR);
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            githubAccessToken,
            java.time.Instant.now(),
            java.time.Instant.now().plusSeconds(60)
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }
}
