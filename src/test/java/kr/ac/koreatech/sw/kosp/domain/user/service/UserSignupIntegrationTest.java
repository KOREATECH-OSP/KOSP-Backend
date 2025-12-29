package kr.ac.koreatech.sw.kosp.domain.user.service;


import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertAll;

class UserSignupIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GithubUserRepository githubUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Roles
        if (roleRepository.findByName("ROLE_STUDENT").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_STUDENT").build());
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        }

        // GithubUsers
        // 123456L for success
        githubUserRepository.save(GithubUser.builder().githubId(123456L).githubLogin("login1").githubName("name1").build());
        
        // 999L for duplicate
        githubUserRepository.save(GithubUser.builder().githubId(999L).githubLogin("login2").githubName("name2").build());
        
        // 888L for reactivation
        githubUserRepository.save(GithubUser.builder().githubId(888L).githubLogin("login3").githubName("name3").build());
        // 123L for invalid (used in failure test but also needs existence to bypass GithubUser check? No, invalid input fails validate BEFORE service)
        // Actually UserService.signup calls getByGithubId BEFORE validation? No, Controller calls @Valid first.
        // So invalid input test doesn't reach service, so GithubUser not needed.
    }

    @Test
    @DisplayName("회원가입 API 성공 - 기본 권한 할당 확인")
    void signup_success_mockMvc() throws Exception {
        // given
        UserSignupRequest request = new UserSignupRequest(
            "testUser",
            "2020136000",
            "test@koreatech.ac.kr",
            "a".repeat(64),
            123456L
        );

        // when
        mockMvc.perform(post("/v1/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated()); // or isCreated() depending on controller

        // then
        User savedUser = userRepository.findByKutEmail("test@koreatech.ac.kr").orElseThrow();
        
        assertAll(
            () -> assertThat(savedUser.getName()).isEqualTo("testUser"),
            () -> assertThat(passwordEncoder.matches("a".repeat(64), savedUser.getPassword())).isTrue(),
            () -> assertThat(savedUser.getRoles()).hasSize(1),
            () -> assertThat(savedUser.getRoles().iterator().next().getName()).isEqualTo("ROLE_STUDENT")
        );
    }

    @Test
    @DisplayName("회원가입 API 실패 - 중복 이메일 (409 Conflict)")
    void signup_fail_duplicate_mockMvc() throws Exception {
        // given: Pre-exist user
        UserSignupRequest request = new UserSignupRequest(
            "dupUser", "2020136999", "dup@koreatech.ac.kr", "a".repeat(64), 999L
        );
        // Create first user via MockMvc or Service
        mockMvc.perform(post("/v1/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // when & then: Signup again
        mockMvc.perform(post("/v1/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("회원가입 API - 탈퇴 계정 복구")
    void signup_reactivation_mockMvc() throws Exception {
        // given: Create and Delete a user (ADMIN role setup)
        UserSignupRequest adminReq = new UserSignupRequest(
            "adminUser", "2020136888", "admin@koreatech.ac.kr", "a".repeat(64), 888L
        );
        mockMvc.perform(post("/v1/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReq)))
            .andExpect(status().isCreated());
            
        User user = userRepository.findByKutEmail("admin@koreatech.ac.kr").orElseThrow();
        user.getRoles().add(roleRepository.findByName("ROLE_ADMIN").orElseThrow());
        user.delete();
        userRepository.save(user);

        // when: Signup again
        UserSignupRequest newReq = new UserSignupRequest(
            "newUser", "2020136888", "admin@koreatech.ac.kr", "b".repeat(64), 888L
        );

        mockMvc.perform(post("/v1/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newReq)))
            .andDo(print())
            .andExpect(status().isCreated());

        // then
        User reactivated = userRepository.findByKutEmail("admin@koreatech.ac.kr").orElseThrow();
        assertAll(
            () -> assertThat(reactivated.isDeleted()).isFalse(),
            () -> assertThat(reactivated.getName()).isEqualTo("adminUser"),
            () -> assertThat(reactivated.getRoles()).extracting("name").containsOnly("ROLE_STUDENT")
        );
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 실패 (400)")
    void signup_fail_invalidInput() throws Exception {
        // given: Invalid Email & Empty Name
        UserSignupRequest request = new UserSignupRequest(
            "", "2020", "invalid-email", "pw", 123L
        );

        // when & then
        mockMvc.perform(post("/v1/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
