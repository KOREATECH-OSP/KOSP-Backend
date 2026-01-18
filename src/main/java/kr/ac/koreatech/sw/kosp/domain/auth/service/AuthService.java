package kr.ac.koreatech.sw.kosp.domain.auth.service;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthMeResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.oauth2.service.OAuth2UserService;
import kr.ac.koreatech.sw.kosp.domain.mail.model.EmailVerification;
import kr.ac.koreatech.sw.kosp.domain.mail.service.EmailVerificationService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.auth.repository.RefreshTokenRepository;
import kr.ac.koreatech.sw.kosp.global.auth.token.AccessToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.JwtToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.RefreshToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.SignupToken;
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
    private final EmailVerificationService emailVerificationService;
    private final OAuth2UserService oAuth2UserService;
    private final UserRepository userRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final TextEncryptor textEncryptor;
    private final RefreshTokenRepository refreshTokenRepository;

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
        SignupToken oldToken = JwtToken.from(SignupToken.class, signupToken);
        JwtToken newToken = oldToken.withEmailVerified(email);
        return newToken.toString();
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

        // 이미 가입된 사용자인지 확인 (탈퇴한 사용자 제외)
        if (userRepository.findByGithubUser_GithubIdAndIsDeletedFalse(githubId).isPresent()) {
            throw new GlobalException(ExceptionMessage.GITHUB_USER_ALREADY_EXISTS);
        }

        // GitHub Access Token 암호화
        String encryptedToken = textEncryptor.encrypt(githubAccessToken);

        // SignupToken 생성
        SignupToken token = SignupToken.fromGithub(
            String.valueOf(githubId),
            oAuth2User.getAttribute("login"),
            oAuth2User.getAttribute("name"),
            oAuth2User.getAttribute("avatar_url"),
            encryptedToken
        );

        return token.toString();
    }

    /**
     * 일반 로그인 (이메일 + 비밀번호)
     */
    public AuthTokenResponse login(LoginRequest request) {
        log.info("🔐 Login attempt for email: {}", request.email());
        Authentication authentication = authenticate(request.email(), request.password());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User)authentication.getPrincipal();
        log.info("✅ Login successful for user: {} (ID: {})", user.getKutEmail(), user.getId());
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
        String profileImage = extractProfileImage(user);

        return new AuthMeResponse(
            user.getId(),
            user.getKutEmail(),
            user.getName(),
            profileImage,
            user.getIntroduction()
        );
    }

    private String extractProfileImage(User user) {
        if (user.getGithubUser() == null) {
            return null;
        }
        return user.getGithubUser().getGithubAvatarUrl();
    }

    /**
     * User Entity 기반으로 토큰 생성
     */
    public AuthTokenResponse createTokensForUser(User user) {
        log.info("🎫 Creating tokens for user: {} (ID: {})", user.getKutEmail(), user.getId());
        return createTokenResponse(user);
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public AuthTokenResponse reissue(RefreshToken refreshToken) {
        // ✅ JWT 검증은 이미 완료 (JwtToken.from()에서)

        // ✅ Level 3: Redis 검증 (비즈니스 로직)
        refreshTokenRepository.verifyExists(refreshToken);

        // 새 토큰 생성
        User user = userRepository.findById(refreshToken.getUserId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.AUTHENTICATION));

        AccessToken newAccessToken = AccessToken.from(user);
        RefreshToken newRefreshToken = RefreshToken.from(user);

        // Redis 업데이트
        refreshTokenRepository.save(newRefreshToken);

        return new AuthTokenResponse(newAccessToken.toString(), newRefreshToken.toString());
    }

    @Transactional
    public void logout(Long userId) {
        RefreshToken token = RefreshToken.builder()
            .userId(userId)
            .build();
        refreshTokenRepository.delete(token);
    }

    private Authentication authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(token);
    }

    private AuthTokenResponse createTokenResponse(User user) {
        // DTO 스타일 토큰 생성
        AccessToken accessToken = AccessToken.from(user);
        RefreshToken refreshToken = RefreshToken.from(user);

        // Redis에 RefreshToken 저장
        refreshTokenRepository.save(refreshToken);

        return new AuthTokenResponse(accessToken.toString(), refreshToken.toString());
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
