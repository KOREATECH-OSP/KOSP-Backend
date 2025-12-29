package kr.ac.koreatech.sw.kosp.domain.admin.service;

import jakarta.servlet.http.HttpSession;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;

import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUserIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private MockHttpSession adminSession;
    private MockHttpSession userSession;
    private User normalUser;

    @BeforeEach
    void setup() throws Exception {
        createRole("ROLE_ADMIN");
        createRole("ROLE_STUDENT");
        createGithubUser(100L);
        createGithubUser(200L);

        // 1. Create Admin
        UserSignupRequest adminReq = new UserSignupRequest(
            "admin", "2020000000", "admin@koreatech.ac.kr", getValidPassword(), 100L
        );
        userService.signup(adminReq);
        User admin = userRepository.findByKutEmail("admin@koreatech.ac.kr").orElseThrow();
        kr.ac.koreatech.sw.kosp.domain.auth.model.Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        // Login Admin
        LoginRequest loginAdmin = new LoginRequest("admin@koreatech.ac.kr", getValidPassword());
        MvcResult adminResult = mockMvc.perform(post("/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginAdmin))).andReturn();
        adminSession = (MockHttpSession) adminResult.getRequest().getSession();

        // 2. Create Normal User
        UserSignupRequest userReq = new UserSignupRequest(
            "user", "2020111111", "user@koreatech.ac.kr", getValidPassword(), 200L
        );
        userService.signup(userReq);
        normalUser = userRepository.findByKutEmail("user@koreatech.ac.kr").orElseThrow();

        // Login User
        LoginRequest loginUser = new LoginRequest("user@koreatech.ac.kr", getValidPassword());
        MvcResult userResult = mockMvc.perform(post("/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginUser))).andReturn();
        userSession = (MockHttpSession) userResult.getRequest().getSession();
    }

    @Test
    @DisplayName("관리자 - 사용자 검색 (목록 조회 대체)")
    void searchUsers_success() throws Exception {
        mockMvc.perform(get("/v1/admin/search")
                .param("keyword", "user")
                .param("type", "USER")
                .session(adminSession))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자 - 사용자 정보 강제 변경")
    void updateUser_success() throws Exception {
        kr.ac.koreatech.sw.kosp.domain.admin.dto.request.AdminUserUpdateRequest req = new kr.ac.koreatech.sw.kosp.domain.admin.dto.request.AdminUserUpdateRequest("ChangedByAdmin", "Forced Update", null);

        mockMvc.perform(put("/v1/admin/users/" + normalUser.getId())
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isOk());

        User updated = userRepository.findById(normalUser.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("ChangedByAdmin");
    }

    @Test
    @DisplayName("관리자 - 사용자 강제 탈퇴")
    void deleteUser_success() throws Exception {
        mockMvc.perform(delete("/v1/admin/users/" + normalUser.getId())
                .session(adminSession))
            .andDo(print())
            .andExpect(status().isNoContent());

        User deleted = userRepository.findById(normalUser.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("권한 검증 - 일반 사용자가 관리자 API 호출 시 403")
    void accessDenied_forNormalUser() throws Exception {
        mockMvc.perform(get("/v1/admin/search")
                .param("keyword", "test")
                .session(userSession))
            .andExpect(status().isForbidden());
    }
}
