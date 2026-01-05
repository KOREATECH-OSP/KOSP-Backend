package kr.ac.koreatech.sw.kosp.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailVerificationRequest;
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
import kr.ac.koreatech.sw.kosp.global.auth.provider.LoginTokenProvider;
import kr.ac.koreatech.sw.kosp.global.auth.provider.SignupTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("인증 통합 테스트")
class AuthProviderIntegrationTest {

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
    private AuthService authService;

    @Autowired
    private LoginTokenProvider loginTokenProvider;

    @Autowired
    private SignupTokenProvider signupTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String VALID_PASSWORD = "Password123!";
    private static final Long TEST_GITHUB_ID = 12345L;

    @BeforeEach
    void setUp() {
        // Create roles
        if (roleRepository.findByName("ROLE_STUDENT").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_STUDENT").build());
        }

        // Create test GitHub user
        if (githubUserRepository.findByGithubId(TEST_GITHUB_ID).isEmpty()) {
            githubUserRepository.save(GithubUser.builder()
                .githubId(TEST_GITHUB_ID)
                .githubLogin("testuser")
                .githubName("Test User")
                .githubAvatarUrl("https://avatar.url")
                .githubToken("encrypted_token")
                .build());
        }

        // Create test user for login tests
        if (userRepository.findByKutEmail("test@koreatech.ac.kr").isEmpty()) {
            User user = User.builder()
                .name("TestUser")
                .kutId("2020136000")
                .kutEmail("test@koreatech.ac.kr")
                .password(VALID_PASSWORD)
                .build();
            user.encodePassword(passwordEncoder);
            user.updateGithubUser(githubUserRepository.getByGithubId(TEST_GITHUB_ID));
            user.getRoles().add(roleRepository.findByName("ROLE_STUDENT").orElseThrow());
            userRepository.save(user);
        }
    }

    @Nested
    @DisplayName("일반 로그인 플로우")
    class LoginFlow {

        @Test
        @DisplayName("성공: 올바른 이메일과 비밀번호로 로그인")
        void login_success() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@koreatech.ac.kr", VALID_PASSWORD);

            // when & then
            MvcResult result = mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

            // Verify tokens are valid
            String response = result.getResponse().getContentAsString();
            AuthTokenResponse tokenResponse = objectMapper.readValue(response, AuthTokenResponse.class);
            
            assertAll(
                () -> assertThat(tokenResponse.accessToken()).isNotEmpty(),
                () -> assertThat(tokenResponse.refreshToken()).isNotEmpty(),
                () -> assertThat(loginTokenProvider.convertAuthToken(tokenResponse.accessToken()).validate()).isTrue(),
                () -> assertThat(loginTokenProvider.convertAuthToken(tokenResponse.refreshToken()).validate()).isTrue()
            );
        }

        @Test
        @DisplayName("실패: 잘못된 비밀번호")
        void login_fail_wrongPassword() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@koreatech.ac.kr", "wrongpassword");

            // when & then
            mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자")
        void login_fail_userNotFound() throws Exception {
            // given
            LoginRequest request = new LoginRequest("nonexistent@koreatech.ac.kr", VALID_PASSWORD);

            // when & then
            mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패: 잘못된 이메일 형식")
        void login_fail_invalidEmailFormat() throws Exception {
            // given
            LoginRequest request = new LoginRequest("invalid-email", VALID_PASSWORD);

            // when & then
            mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("토큰 재발급 플로우")
    class TokenReissueFlow {

        @Test
        @DisplayName("성공: 유효한 Refresh Token으로 Access Token 재발급")
        void reissue_success() throws Exception {
            // given: 로그인하여 토큰 발급
            LoginRequest loginRequest = new LoginRequest("test@koreatech.ac.kr", VALID_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            AuthTokenResponse loginTokens = objectMapper.readValue(loginResponse, AuthTokenResponse.class);

            // Wait a few milliseconds to ensure different token generation time
            Thread.sleep(1000);

            // when & then: Refresh Token으로 재발급
            Map<String, String> reissueRequest = Map.of("refreshToken", loginTokens.refreshToken());
            
            MvcResult reissueResult = mockMvc.perform(post("/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reissueRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

            String reissueResponse = reissueResult.getResponse().getContentAsString();
            AuthTokenResponse newTokens = objectMapper.readValue(reissueResponse, AuthTokenResponse.class);

            assertAll(
                () -> assertThat(newTokens.accessToken()).isNotEmpty(),
                () -> assertThat(newTokens.accessToken()).isNotEqualTo(loginTokens.accessToken()),
                () -> assertThat(newTokens.refreshToken()).isEqualTo(loginTokens.refreshToken())
            );
        }

        @Test
        @DisplayName("실패: 잘못된 Refresh Token")
        void reissue_fail_invalidToken() throws Exception {
            // given
            Map<String, String> request = Map.of("refreshToken", "invalid.token.here");

            // when & then
            mockMvc.perform(post("/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패: Redis에 없는 Refresh Token")
        void reissue_fail_tokenNotInRedis() throws Exception {
            // given: 로그인 후 로그아웃하여 Redis에서 토큰 삭제
            LoginRequest loginRequest = new LoginRequest("test@koreatech.ac.kr", VALID_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            AuthTokenResponse tokens = objectMapper.readValue(loginResponse, AuthTokenResponse.class);

            // 로그아웃 (Redis에서 토큰 삭제)
            User user = userRepository.findByKutEmail("test@koreatech.ac.kr").orElseThrow();
            authService.logout(user.getId());

            // when & then: 삭제된 토큰으로 재발급 시도
            Map<String, String> request = Map.of("refreshToken", tokens.refreshToken());
            
            mockMvc.perform(post("/v1/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("로그아웃 플로우")
    class LogoutFlow {

        @Test
        @DisplayName("성공: 로그인 후 로그아웃")
        void logout_success() throws Exception {
            // given: 로그인
            LoginRequest loginRequest = new LoginRequest("test@koreatech.ac.kr", VALID_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            AuthTokenResponse tokens = objectMapper.readValue(loginResponse, AuthTokenResponse.class);

            User user = userRepository.findByKutEmail("test@koreatech.ac.kr").orElseThrow();
            String refreshKey = "refresh:" + user.getId();
            
            // Verify token exists in Redis
            assertThat(redisTemplate.opsForValue().get(refreshKey)).isNotNull();

            // when: 로그아웃
            mockMvc.perform(post("/v1/auth/logout")
                    .header("Authorization", "Bearer " + tokens.accessToken()))
                .andDo(print())
                .andExpect(status().isOk());

            // then: Redis에서 토큰 삭제 확인
            assertThat(redisTemplate.opsForValue().get(refreshKey)).isNull();
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자")
        void logout_fail_unauthorized() throws Exception {
            mockMvc.perform(post("/v1/auth/logout"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("이메일 인증 플로우")
    class EmailVerificationFlow {

        private String signupToken;

        @BeforeEach
        void setupSignupToken() {
            // Create a valid signup token
            Map<String, Object> claims = new HashMap<>();
            claims.put("login", "newuser");
            claims.put("name", "New User");
            claims.put("avatar_url", "https://avatar.url");
            claims.put("email", "newuser@example.com");
            claims.put("encryptedGithubToken", "encrypted_token_here");
            
            signupToken = ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) 
                signupTokenProvider.createSignupToken("99999", claims)).getToken();
        }

        @Test
        @DisplayName("성공: 이메일 인증 코드 발송")
        void sendVerificationCode_success() throws Exception {
            // given
            EmailRequest request = new EmailRequest("newuser@koreatech.ac.kr", signupToken);

            // when & then
            mockMvc.perform(post("/v1/auth/verify/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

            // Verify Redis storage
            EmailVerification verification = emailVerificationRepository.findById("newuser@koreatech.ac.kr").orElseThrow();
            assertAll(
                () -> assertThat(verification.getCode()).hasSize(6),
                () -> assertThat(verification.getSignupToken()).isEqualTo(signupToken),
                () -> assertThat(verification.isVerified()).isFalse()
            );
        }

        @Test
        @DisplayName("성공: 이메일 인증 코드 확인")
        void verifyCode_success() throws Exception {
            // given: 인증 코드 발송
            EmailRequest emailRequest = new EmailRequest("newuser@koreatech.ac.kr", signupToken);
            mockMvc.perform(post("/v1/auth/verify/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk());

            EmailVerification verification = emailVerificationRepository.findById("newuser@koreatech.ac.kr").orElseThrow();
            String code = verification.getCode();

            // when & then: 코드 확인
            EmailVerificationRequest verifyRequest = new EmailVerificationRequest("newuser@koreatech.ac.kr", code);
            
            MvcResult result = mockMvc.perform(post("/v1/auth/verify/email/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signupToken").exists())
                .andReturn();

            // Verify new token contains email and emailVerified
            String response = result.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, String> responseMap = objectMapper.readValue(response, Map.class);
            String newSignupToken = responseMap.get("signupToken");

            var token = signupTokenProvider.parseSignupToken(newSignupToken);
            var claims = token.getData();
            
            assertAll(
                () -> assertThat(claims.get("kutEmail")).isEqualTo("newuser@koreatech.ac.kr"),
                () -> assertThat(claims.get("emailVerified")).isEqualTo(true)
            );
        }

        @Test
        @DisplayName("실패: 잘못된 인증 코드")
        void verifyCode_fail_wrongCode() throws Exception {
            // given: 인증 코드 발송
            EmailRequest emailRequest = new EmailRequest("newuser@koreatech.ac.kr", signupToken);
            mockMvc.perform(post("/v1/auth/verify/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk());

            // when & then: 잘못된 코드로 확인 시도
            EmailVerificationRequest verifyRequest = new EmailVerificationRequest("newuser@koreatech.ac.kr", "000000");
            
            mockMvc.perform(post("/v1/auth/verify/email/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일")
        void verifyCode_fail_emailNotFound() throws Exception {
            // given
            EmailVerificationRequest request = new EmailVerificationRequest("nonexistent@koreatech.ac.kr", "123456");

            // when & then
            mockMvc.perform(post("/v1/auth/verify/email/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("회원가입 플로우")
    class SignupFlow {

        @Test
        @DisplayName("성공: 이메일 인증 완료 후 회원가입")
        void signup_success_withEmailVerification() throws Exception {
            // given: 이메일 인증이 완료된 signup token 생성
            Map<String, Object> claims = new HashMap<>();
            claims.put("login", "signupuser");
            claims.put("name", "Signup User");
            claims.put("avatar_url", "https://avatar.url");
            claims.put("email", "signupuser@example.com");
            claims.put("encryptedGithubToken", "encrypted_token_here");
            claims.put("kutEmail", "signup@koreatech.ac.kr");
            claims.put("emailVerified", true);
            claims.put("category", "SIGNUP");
            
            // Create GitHub user first
            Long newGithubId = 88888L;
            githubUserRepository.save(GithubUser.builder()
                .githubId(newGithubId)
                .githubLogin("signupuser")
                .githubName("Signup User")
                .build());

            String verifiedSignupToken = ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) 
                signupTokenProvider.createSignupToken(String.valueOf(newGithubId), claims)).getToken();

            UserSignupRequest signupRequest = new UserSignupRequest(
                "Signup User",
                "2020136888",
                "signup@koreatech.ac.kr",
                VALID_PASSWORD,
                verifiedSignupToken
            );

            // when & then
            mockMvc.perform(post("/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

            // Verify user created
            User user = userRepository.findByKutEmail("signup@koreatech.ac.kr").orElseThrow();
            assertAll(
                () -> assertThat(user.getName()).isEqualTo("Signup User"),
                () -> assertThat(user.getKutId()).isEqualTo("2020136888"),
                () -> assertThat(user.getGithubUser().getGithubId()).isEqualTo(newGithubId),
                () -> assertThat(user.getRoles()).hasSize(1),
                () -> assertThat(user.getRoles().iterator().next().getName()).isEqualTo("ROLE_STUDENT")
            );
        }

        @Test
        @DisplayName("실패: 이메일 인증되지 않은 토큰")
        void signup_fail_emailNotVerified() throws Exception {
            // given: emailVerified가 false인 토큰
            Map<String, Object> claims = new HashMap<>();
            claims.put("login", "unverified");
            claims.put("name", "Unverified User");
            claims.put("encryptedGithubToken", "encrypted_token_here");
            claims.put("category", "SIGNUP");
            
            String unverifiedToken = ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) 
                signupTokenProvider.createSignupToken("77777", claims)).getToken();

            UserSignupRequest request = new UserSignupRequest(
                "Unverified User",
                "2020136777",
                "unverified@koreatech.ac.kr",
                VALID_PASSWORD,
                unverifiedToken
            );

            // when & then
            mockMvc.perform(post("/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 중복된 이메일")
        void signup_fail_duplicateEmail() throws Exception {
            // given: 이미 존재하는 사용자
            Map<String, Object> claims = new HashMap<>();
            claims.put("login", "duplicate");
            claims.put("name", "Duplicate User");
            claims.put("encryptedGithubToken", "encrypted_token_here");
            claims.put("kutEmail", "test@koreatech.ac.kr"); // Already exists
            claims.put("emailVerified", true);
            claims.put("category", "SIGNUP");
            
            String duplicateToken = ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) 
                signupTokenProvider.createSignupToken(String.valueOf(TEST_GITHUB_ID), claims)).getToken();

            UserSignupRequest request = new UserSignupRequest(
                "Duplicate User",
                "2020136999",
                "test@koreatech.ac.kr",
                VALID_PASSWORD,
                duplicateToken
            );

            // when & then
            mockMvc.perform(post("/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
        }
    }
}
