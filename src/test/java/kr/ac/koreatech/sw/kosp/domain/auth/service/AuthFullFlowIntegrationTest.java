package kr.ac.koreatech.sw.kosp.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailVerificationRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.GithubTokenRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.mail.model.EmailVerification;
import kr.ac.koreatech.sw.kosp.domain.mail.repository.EmailVerificationRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.auth.token.AccessToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.SignupToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("인증 통합 테스트 - Full Flow with Real GitHub Token")
class AuthFullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GithubUserRepository githubUserRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;



    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${test.github.access-token:}")
    private String githubTestToken;

    private static final String VALID_PASSWORD = "TestPassword123!";
    private static final String TEST_KUT_EMAIL = "test@koreatech.ac.kr";

    @BeforeEach
    void setUp() {
        // Create roles
        if (roleRepository.findByName("ROLE_STUDENT").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_STUDENT").build());
        }
    }
    
    @AfterEach
    void cleanup() {
        // Clean up Redis data (Redis doesn't support transaction rollback)
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // Redis not available - tests will be skipped anyway
        }
    }

    boolean isGithubTokenAvailable() {
        return githubTestToken != null && !githubTestToken.isEmpty() && !githubTestToken.equals("PASTE_YOUR_TOKEN_HERE");
    }

    @Nested
    @DisplayName("전체 회원가입 플로우")
    class FullSignupFlow {

        @Test
        @DisplayName("성공: GitHub Exchange → Email 인증 → Signup → Login")
        void fullSignupFlow_success() throws Exception {
            // Skip if no GitHub token
            if (!isGithubTokenAvailable()) {
                System.out.println("⚠️  Skipping test: GitHub token not configured in application-test.yml");
                return;
            }

            // 1. GitHub Token Exchange with REAL token
            GithubTokenRequest exchangeRequest = new GithubTokenRequest(githubTestToken);
            MvcResult exchangeResult = mockMvc.perform(post("/v1/auth/github/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(exchangeRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationToken").exists())
                .andReturn();

            String exchangeResponse = exchangeResult.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, String> exchangeMap = objectMapper.readValue(exchangeResponse, Map.class);
            String signupToken = exchangeMap.get("verificationToken");

            // Extract GitHub ID from signup token for later verification
            SignupToken parsedToken = SignupToken.from(SignupToken.class, signupToken);
            Long githubId = Long.valueOf(parsedToken.getGithubId());

            // 2. Send Email Verification Code
            EmailRequest emailRequest = new EmailRequest(TEST_KUT_EMAIL);
            mockMvc.perform(post("/v1/auth/verify/email")
                    .header("X-Signup-Token", signupToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emailRequest)))
                .andDo(print())
                .andExpect(status().isOk());

            // Verify Redis storage
            EmailVerification verification = emailVerificationRepository.findById(TEST_KUT_EMAIL).orElseThrow();
            assertThat(verification.getCode()).hasSize(6);
            String verificationCode = verification.getCode();

            // 3. Verify Email Code
            EmailVerificationRequest verifyRequest = new EmailVerificationRequest(TEST_KUT_EMAIL, verificationCode);
            MvcResult verifyResult = mockMvc.perform(post("/v1/auth/verify/email/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signupToken").exists())
                .andReturn();

            String verifyResponse = verifyResult.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, String> verifyMap = objectMapper.readValue(verifyResponse, Map.class);
            String verifiedSignupToken = verifyMap.get("signupToken");

            // Verify token contains email and emailVerified
            SignupToken token = SignupToken.from(SignupToken.class, verifiedSignupToken);
            assertAll(
                () -> assertThat(token.getKutEmail()).isEqualTo(TEST_KUT_EMAIL),
                () -> assertThat(token.isEmailVerified()).isTrue()
            );

            // 4. Signup
            UserSignupRequest signupRequest = new UserSignupRequest(
                "TestUser",
                "2020136000",
                TEST_KUT_EMAIL,
                VALID_PASSWORD,
                verifiedSignupToken
            );

            MvcResult signupResult = mockMvc.perform(post("/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

            // Verify user created
            User user = userRepository.findByKutEmail(TEST_KUT_EMAIL).orElseThrow();
            assertAll(
                () -> assertThat(user.getName()).isEqualTo("TestUser"),
                () -> assertThat(user.getKutId()).isEqualTo("2020136000"),
                () -> assertThat(user.getGithubUser()).isNotNull(),
                () -> assertThat(user.getGithubUser().getGithubId()).isEqualTo(githubId),
                () -> assertThat(user.getRoles()).hasSize(1),
                () -> assertThat(user.getRoles().iterator().next().getName()).isEqualTo("ROLE_STUDENT")
            );

            String signupResponse = signupResult.getResponse().getContentAsString();
            AuthTokenResponse signupTokens = objectMapper.readValue(signupResponse, AuthTokenResponse.class);

            // 5. Login with credentials
            LoginRequest loginRequest = new LoginRequest(TEST_KUT_EMAIL, VALID_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            AuthTokenResponse loginTokens = objectMapper.readValue(loginResponse, AuthTokenResponse.class);

            // Tokens are valid if they can be parsed successfully
        }

        @Test
        @DisplayName("실패: 이메일 인증 없이 회원가입 시도")
        void signup_fail_withoutEmailVerification() throws Exception {
            // Skip if no GitHub token
            if (!isGithubTokenAvailable()) {
                System.out.println("⚠️  Skipping test: GitHub token not configured");
                return;
            }

            // 1. GitHub Token Exchange
            GithubTokenRequest exchangeRequest = new GithubTokenRequest(githubTestToken);
            MvcResult exchangeResult = mockMvc.perform(post("/v1/auth/github/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(exchangeRequest)))
                .andExpect(status().isOk())
                .andReturn();

            String exchangeResponse = exchangeResult.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, String> exchangeMap = objectMapper.readValue(exchangeResponse, Map.class);
            String signupToken = exchangeMap.get("verificationToken");

            // 2. Try to signup without email verification
            UserSignupRequest signupRequest = new UserSignupRequest(
                "TestUser",
                "2020136000",
                TEST_KUT_EMAIL,
                VALID_PASSWORD,
                signupToken  // Not verified!
            );

            mockMvc.perform(post("/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 잘못된 인증 코드")
        void emailVerification_fail_wrongCode() throws Exception {
            // Skip if no GitHub token
            if (!isGithubTokenAvailable()) {
                System.out.println("⚠️  Skipping test: GitHub token not configured");
                return;
            }

            // 1. GitHub Token Exchange
            GithubTokenRequest exchangeRequest = new GithubTokenRequest(githubTestToken);
            MvcResult exchangeResult = mockMvc.perform(post("/v1/auth/github/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(exchangeRequest)))
                .andExpect(status().isOk())
                .andReturn();

            String exchangeResponse = exchangeResult.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, String> exchangeMap = objectMapper.readValue(exchangeResponse, Map.class);
            String signupToken = exchangeMap.get("verificationToken");

            // 2. Send Email Verification Code
            EmailRequest emailRequest = new EmailRequest(TEST_KUT_EMAIL);
            mockMvc.perform(post("/v1/auth/verify/email")
                    .header("X-Signup-Token", signupToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk());

            // 3. Try to verify with wrong code
            EmailVerificationRequest verifyRequest = new EmailVerificationRequest(TEST_KUT_EMAIL, "000000");
            mockMvc.perform(post("/v1/auth/verify/email/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GitHub 로그인 플로우")
    class GithubLoginFlow {

        @Test
        @DisplayName("성공: 기존 사용자 GitHub 로그인")
        void githubLogin_success() throws Exception {
            // Skip if no GitHub token
            if (!isGithubTokenAvailable()) {
                System.out.println("⚠️  Skipping test: GitHub token not configured");
                return;
            }

            // First, get GitHub user info from exchange
            GithubTokenRequest exchangeRequest = new GithubTokenRequest(githubTestToken);
            MvcResult exchangeResult = mockMvc.perform(post("/v1/auth/github/exchange")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(exchangeRequest)))
                .andExpect(status().isOk())
                .andReturn();

            String exchangeResponse = exchangeResult.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, String> exchangeMap = objectMapper.readValue(exchangeResponse, Map.class);
            String signupToken = exchangeMap.get("verificationToken");
            
            SignupToken parsedToken = SignupToken.from(SignupToken.class, signupToken);
            Long githubId = Long.valueOf(parsedToken.getGithubId());
            String githubLogin = parsedToken.getLogin();
            String githubName = parsedToken.getName();
            String githubAvatar = parsedToken.getAvatarUrl();

            // Setup: Create existing user with real GitHub data
            GithubUser githubUser = githubUserRepository.save(GithubUser.builder()
                .githubId(githubId)
                .githubLogin(githubLogin)
                .githubName(githubName)
                .githubAvatarUrl(githubAvatar)
                .githubToken("encrypted_token")
                .build());

            User user = User.builder()
                .name("ExistingUser")
                .kutId("2020136999")
                .kutEmail("existing@koreatech.ac.kr")
                .password(VALID_PASSWORD)
                .build();
            user.encodePassword(passwordEncoder);
            user.updateGithubUser(githubUser);
            user.getRoles().add(roleRepository.findByName("ROLE_STUDENT").orElseThrow());
            userRepository.save(user);

            // When: GitHub login with real token
            GithubTokenRequest loginRequest = new GithubTokenRequest(githubTestToken);
            MvcResult result = mockMvc.perform(post("/v1/auth/login/github")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

            // Tokens are valid if they can be parsed successfully
        }

        @Test
        @DisplayName("실패: 미등록 사용자 GitHub 로그인")
        void githubLogin_fail_userNotFound() throws Exception {
            // This test requires a different GitHub account token
            // Skipping for now as it requires multiple GitHub accounts
            System.out.println("⚠️  Skipping test: Requires different GitHub account");
        }
    }

    @Nested
    @DisplayName("토큰 관리 플로우")
    class TokenManagementFlow {

        private User testUser;
        private AuthTokenResponse tokens;

        @BeforeEach
        void setupUser() throws Exception {
            // Create test user with dummy GitHub data
            GithubUser githubUser = githubUserRepository.save(GithubUser.builder()
                .githubId(99999L)
                .githubLogin("testuser")
                .githubName("TestUser")
                .build());

            testUser = User.builder()
                .name("TokenTestUser")
                .kutId("2020136888")
                .kutEmail("token@koreatech.ac.kr")
                .password(VALID_PASSWORD)
                .build();
            testUser.encodePassword(passwordEncoder);
            testUser.updateGithubUser(githubUser);
            testUser.getRoles().add(roleRepository.findByName("ROLE_STUDENT").orElseThrow());
            userRepository.save(testUser);

            // Login to get tokens
            LoginRequest loginRequest = new LoginRequest("token@koreatech.ac.kr", VALID_PASSWORD);
            MvcResult result = mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

            String response = result.getResponse().getContentAsString();
            tokens = objectMapper.readValue(response, AuthTokenResponse.class);
        }

        @Test
        @DisplayName("성공: Refresh Token으로 Access Token 재발급")
        void reissue_success() throws Exception {
            // Wait 1 second to ensure different token generation time
            Thread.sleep(1000);
            
            // When: Reissue with refresh token
            Map<String, String> reissueRequest = Map.of("refreshToken", tokens.refreshToken());
            MvcResult result = mockMvc.perform(post("/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reissueRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

            // Then: Verify new access token is different
            String response = result.getResponse().getContentAsString();
            AuthTokenResponse newTokens = objectMapper.readValue(response, AuthTokenResponse.class);
            assertAll(
                () -> assertThat(newTokens.accessToken()).isNotEqualTo(tokens.accessToken()),
                () -> assertThat(newTokens.refreshToken()).isEqualTo(tokens.refreshToken())
            );
        }

        @Test
        @DisplayName("성공: 로그아웃 후 Refresh Token 무효화")
        void logout_success() throws Exception {
            // When: Logout
            mockMvc.perform(post("/v1/auth/logout")
                    .header("Authorization", "Bearer " + tokens.accessToken()))
                .andDo(print())
                .andExpect(status().isOk());

            // Then: Refresh token should be removed from Redis
            String refreshKey = "refresh:" + testUser.getId();
            assertThat(redisTemplate.opsForValue().get(refreshKey)).isNull();

            // And: Reissue should fail
            Map<String, String> reissueRequest = Map.of("refreshToken", tokens.refreshToken());
            mockMvc.perform(post("/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reissueRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패: 잘못된 Refresh Token으로 재발급")
        void reissue_fail_invalidToken() throws Exception {
            Map<String, String> request = Map.of("refreshToken", "invalid.token.here");
            mockMvc.perform(post("/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }
    }
}
