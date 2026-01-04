package kr.ac.koreatech.sw.kosp.global.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    protected kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository githubUserRepository;

    @Autowired
    protected kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository roleRepository;

    @Autowired
    protected kr.ac.koreatech.sw.kosp.domain.user.service.UserService userService;
    
    @Autowired
    protected org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    @Autowired
    protected kr.ac.koreatech.sw.kosp.global.auth.provider.SignupTokenProvider signupTokenProvider;

    protected void createRole(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(kr.ac.koreatech.sw.kosp.domain.auth.model.Role.builder().name(name).build());
        }
    }

    protected void createGithubUser(Long id) {
        githubUserRepository.save(kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser.builder().githubId(id).githubLogin("user"+id).githubName("name"+id).build());
    }

    protected String getValidPassword() {
        return "Password123!"; // Meets: letters, numbers, special chars, 8+ chars
    }

    /**
     * Helper method to create a valid signup token for testing
     * Creates a real JWT token that can be parsed by SignupTokenProvider
     */
    protected String createSignupToken(Long githubId, String kutEmail) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("login", "user" + githubId);
        claims.put("name", "name" + githubId);
        claims.put("avatar_url", "https://avatar.url");
        claims.put("email", "github@example.com");
        claims.put("encryptedGithubToken", "encrypted_token_" + githubId);
        claims.put("kutEmail", kutEmail);
        claims.put("emailVerified", true);
        claims.put("category", "SIGNUP");
        
        return ((kr.ac.koreatech.sw.kosp.global.auth.provider.JwtAuthToken) 
            signupTokenProvider.createSignupToken(String.valueOf(githubId), claims)).getToken();
    }
}
