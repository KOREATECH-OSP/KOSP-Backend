package io.swkoreatech.kosp.global.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.domain.auth.dto.request.LoginRequest;
import io.swkoreatech.kosp.domain.auth.dto.response.AuthTokenResponse;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.auth.repository.RoleRepository;
import io.swkoreatech.kosp.domain.github.model.GithubUser;
import io.swkoreatech.kosp.domain.github.repository.GithubUserRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.service.UserService;
import io.swkoreatech.kosp.global.auth.token.AccessToken;
import io.swkoreatech.kosp.global.auth.token.SignupToken;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected GithubUserRepository githubUserRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected UserService userService;
    
    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected void createRole(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder().name(name).build());
        }
    }

    protected void createGithubUser(Long id) {
        githubUserRepository.save(GithubUser.builder()
            .githubId(id)
            .githubLogin("user" + id)
            .githubName("name" + id)
            .githubToken("dummy_token_" + id)
            .githubAvatarUrl("https://dummy.url/" + id)
            .createdAt(java.time.LocalDateTime.now())
            .updatedAt(java.time.LocalDateTime.now())
            .build());
    }

    protected String getValidPassword() {
        return "Password123!";
    }

    /**
     * Helper method to create a valid signup token for testing
     */
    protected String createSignupToken(Long githubId, String kutEmail) {
        SignupToken token = SignupToken.builder()
            .githubId(String.valueOf(githubId))
            .login("user" + githubId)
            .name("name" + githubId)
            .avatarUrl("https://avatar.url")
            .encryptedGithubToken("encrypted_token_" + githubId)
            .kutEmail(kutEmail)
            .emailVerified(true)
            .build();
        
        return token.toString();
    }

    /**
     * 로그인 후 Access Token 반환
     */
    protected String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        AuthTokenResponse tokenResponse = objectMapper.readValue(
            responseBody, AuthTokenResponse.class);
        
        return tokenResponse.accessToken();
    }

    /**
     * User 객체로부터 직접 Access Token 생성 (테스트용)
     */
    protected String createAccessToken(User user) {
        AccessToken token = AccessToken.from(user);
        return token.toString();
    }

    /**
     * Bearer 토큰 헤더 생성
     */
    protected String bearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }
}
