package kr.ac.koreatech.sw.kosp.global.common;

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

import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.auth.token.LoginToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.SignupToken;

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
        LoginToken token = LoginToken.from(user);
        return token.toString();
    }

    /**
     * Bearer 토큰 헤더 생성
     */
    protected String bearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }
}
