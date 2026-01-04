package kr.ac.koreatech.sw.kosp.global.common;

import java.util.HashMap;
import java.util.Map;

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

import io.jsonwebtoken.Claims;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.auth.core.AuthToken;
import kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken;
import kr.ac.koreatech.sw.kosp.global.auth.provider.LoginTokenProvider;
import kr.ac.koreatech.sw.kosp.global.auth.provider.SignupTokenProvider;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    
    @Autowired
    protected SignupTokenProvider signupTokenProvider;
    
    @Autowired
    protected LoginTokenProvider loginTokenProvider;

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
            .build());
    }

    protected String getValidPassword() {
        return "Password123!";
    }

    /**
     * Helper method to create a valid signup token for testing
     */
    protected String createSignupToken(Long githubId, String kutEmail) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("login", "user" + githubId);
        claims.put("name", "name" + githubId);
        claims.put("avatar_url", "https://avatar.url");
        claims.put("email", "github@example.com");
        claims.put("encryptedGithubToken", "encrypted_token_" + githubId);
        claims.put("kutEmail", kutEmail);
        claims.put("emailVerified", true);
        claims.put("category", "SIGNUP");
        
        return ((JwtAuthToken) signupTokenProvider.createSignupToken(
            String.valueOf(githubId), claims)).getToken();
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
        AuthToken<Claims> token = loginTokenProvider.createAccessToken(user);
        return ((JwtAuthToken) token).getToken();
    }

    /**
     * Bearer 토큰 헤더 생성
     */
    protected String bearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }
}
