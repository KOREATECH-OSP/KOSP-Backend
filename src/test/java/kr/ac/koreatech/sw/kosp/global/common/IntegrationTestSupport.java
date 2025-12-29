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

    protected void createRole(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(kr.ac.koreatech.sw.kosp.domain.auth.model.Role.builder().name(name).build());
        }
    }

    protected void createGithubUser(Long id) {
        githubUserRepository.save(kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser.builder().githubId(id).githubLogin("user"+id).githubName("name"+id).build());
    }

    protected String getValidPassword() {
        return "a".repeat(64);
    }
}
