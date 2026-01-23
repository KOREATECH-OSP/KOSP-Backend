package io.swkoreatech.kosp.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.security.Keys;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.domain.auth.dto.request.LoginRequest;
import io.swkoreatech.kosp.domain.auth.dto.response.AuthMeResponse;
import io.swkoreatech.kosp.domain.auth.dto.response.AuthTokenResponse;
import io.swkoreatech.kosp.domain.auth.oauth2.service.OAuth2UserService;
import io.swkoreatech.kosp.domain.mail.model.EmailVerification;
import io.swkoreatech.kosp.domain.mail.service.EmailVerificationService;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.auth.repository.RefreshTokenRepository;
import io.swkoreatech.kosp.global.auth.token.RefreshToken;
import io.swkoreatech.kosp.global.auth.token.TokenType;
import io.swkoreatech.kosp.global.config.jwt.TokenPropertiesProvider;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private OAuth2UserService oAuth2UserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private TextEncryptor textEncryptor;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private MockedStatic<TokenPropertiesProvider> tokenPropertiesProviderMock;

    @BeforeEach
    void setUp() {
        tokenPropertiesProviderMock = mockStatic(TokenPropertiesProvider.class);

        String secretKeyString = "test-secret-key-for-jwt-signing-must-be-256-bits-long!!";
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        ObjectMapper objectMapper = new ObjectMapper();

        tokenPropertiesProviderMock.when(TokenPropertiesProvider::secretKey).thenReturn(secretKey);
        tokenPropertiesProviderMock.when(TokenPropertiesProvider::objectMapper).thenReturn(objectMapper);

        TokenType.ACCESS.setExpiration(3600000L);
        TokenType.REFRESH.setExpiration(86400000L);
    }

    @AfterEach
    void tearDown() {
        tokenPropertiesProviderMock.close();
    }

    private User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Nested
    @DisplayName("sendCertificationMail 메서드")
    class SendCertificationMailTest {

        @Test
        @DisplayName("인증 메일을 발송한다")
        void sendsCertificationMail() {
            // given
            String email = "test@koreatech.ac.kr";
            String signupToken = "signup-token";

            // when
            authService.sendCertificationMail(email, signupToken);

            // then
            verify(emailVerificationService).sendCertificationMail(email, signupToken);
        }
    }

    @Nested
    @DisplayName("verifyCode 메서드")
    class VerifyCodeTest {

        @Test
        @DisplayName("인증 코드가 없으면 null을 반환한다")
        void returnsNull_whenNoSignupToken() {
            // given
            String email = "test@koreatech.ac.kr";
            EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code("123456")
                .signupToken(null)
                .isVerified(true)
                .ttl(300L)
                .build();
            
            given(emailVerificationService.verifyCode(email, "123456")).willReturn(verification);

            // when
            String result = authService.verifyCode(email, "123456");

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("login 메서드")
    class LoginTest {

        @Test
        @DisplayName("이메일과 비밀번호로 로그인에 성공한다")
        void logsInWithCredentials() {
            // given
            User user = createUser(1L, "testuser");
            LoginRequest request = new LoginRequest("test@koreatech.ac.kr", "password");
            Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            
            given(authenticationManager.authenticate(any())).willReturn(auth);

            // when
            AuthTokenResponse response = authService.login(request);

            // then
            assertThat(response.accessToken()).isNotNull();
            assertThat(response.refreshToken()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getUserInfo 메서드")
    class GetUserInfoTest {

        @Test
        @DisplayName("GitHub 사용자가 없으면 프로필 이미지가 null이다")
        void returnsNullProfileImage_whenNoGithubUser() {
            // given
            User user = createUser(1L, "testuser");

            // when
            AuthMeResponse response = authService.getUserInfo(user);

            // then
            assertThat(response.profileImage()).isNull();
            assertThat(response.name()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("GitHub 사용자가 있으면 아바타 URL을 반환한다")
        void returnsAvatarUrl_whenGithubUserExists() {
            // given
            User user = createUser(1L, "testuser");
            GithubUser githubUser = GithubUser.builder()
                .githubId(123L)
                .githubLogin("testlogin")
                .githubAvatarUrl("https://avatar.url")
                .build();
            ReflectionTestUtils.setField(user, "githubUser", githubUser);

            // when
            AuthMeResponse response = authService.getUserInfo(user);

            // then
            assertThat(response.profileImage()).isEqualTo("https://avatar.url");
        }
    }

    @Nested
    @DisplayName("createTokensForUser 메서드")
    class CreateTokensForUserTest {

        @Test
        @DisplayName("사용자에 대한 토큰을 생성한다")
        void createsTokensForUser() {
            // given
            User user = createUser(1L, "testuser");

            // when
            AuthTokenResponse response = authService.createTokensForUser(user);

            // then
            assertThat(response.accessToken()).isNotNull();
            assertThat(response.refreshToken()).isNotNull();
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("reissue 메서드")
    class ReissueTest {

        @Test
        @DisplayName("사용자가 없으면 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            RefreshToken refreshToken = RefreshToken.builder()
                .userId(999L)
                .build();
            
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("새로운 토큰을 발급한다")
        void reissuesTokens() {
            // given
            User user = createUser(1L, "testuser");
            RefreshToken refreshToken = RefreshToken.builder()
                .userId(1L)
                .build();
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            AuthTokenResponse response = authService.reissue(refreshToken);

            // then
            assertThat(response.accessToken()).isNotNull();
            assertThat(response.refreshToken()).isNotNull();
        }
    }

    @Nested
    @DisplayName("logout 메서드")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃 시 RefreshToken을 삭제한다")
        void deletesRefreshToken() {
            // given
            Long userId = 1L;

            // when
            authService.logout(userId);

            // then
            verify(refreshTokenRepository).delete(any(RefreshToken.class));
        }
    }
}
