package kr.ac.koreatech.sw.kosp.domain.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminRoleIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private String adminAccessToken;

    @BeforeEach
    void setup() throws Exception {
        createGithubUser(500L);

        // Create Admin
        String signupToken = createSignupToken(500L, "admin.role@koreatech.ac.kr");
        UserSignupRequest adminReq = new UserSignupRequest(
            "adminRole", "2020000500", "admin.role@koreatech.ac.kr", getValidPassword(), signupToken
        );
        userService.signup(adminReq);
        User admin = userRepository.findByKutEmail("admin.role@koreatech.ac.kr").orElseThrow();
        kr.ac.koreatech.sw.kosp.domain.auth.model.Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        // Login Admin
        adminAccessToken = loginAndGetToken("admin.role@koreatech.ac.kr", getValidPassword());
    }

    @Test
    @DisplayName("역할 목록 조회 성공")
    void getAllRoles_success() throws Exception {
        mockMvc.perform(get("/v1/admin/roles")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.name == 'ROLE_ADMIN')]").exists());
    }

    @Test
    @DisplayName("역할 생성 성공")
    void createRole_success() throws Exception {
        // given
        RoleRequest req = new RoleRequest("ROLE_MANAGER", "매니저");

        // when
        mockMvc.perform(post("/v1/admin/roles")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        // then
        assertThat(roleRepository.findByName("ROLE_MANAGER")).isPresent();
    }
}
