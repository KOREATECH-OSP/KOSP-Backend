package kr.ac.koreatech.sw.kosp.domain.auth.service;

import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setup() {
        createRole("ROLE_STUDENT");
        createGithubUser(111L);

        // Create user for login testing
        UserSignupRequest request = new UserSignupRequest(
            "loginUser", "2020136111", "login@koreatech.ac.kr", getValidPassword(), 111L
        );
        userService.signup(request);
    }

    @Test
    @DisplayName("로그인 성공 - 200 OK 및 JSESSIONID 쿠키 발급")
    void login_success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("login@koreatech.ac.kr", getValidPassword());

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치 (401)")
    void login_fail_wrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("login@koreatech.ac.kr", "b".repeat(64));

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 계정 (401)")
    void login_fail_userNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest("unknown@koreatech.ac.kr", getValidPassword());

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        // given: 로그인
        LoginRequest loginRequest = new LoginRequest("login@koreatech.ac.kr", getValidPassword());
        org.springframework.test.web.servlet.MvcResult result = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        org.springframework.mock.web.MockHttpSession session = (org.springframework.mock.web.MockHttpSession) result.getRequest().getSession();

        // when & then: 로그아웃
        mockMvc.perform(post("/v1/auth/logout")
                .session(session))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_success() throws Exception {
        // given: 로그인
        LoginRequest loginRequest = new LoginRequest("login@koreatech.ac.kr", getValidPassword());
        org.springframework.test.web.servlet.MvcResult result = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        org.springframework.mock.web.MockHttpSession session = (org.springframework.mock.web.MockHttpSession) result.getRequest().getSession();

        // when & then: 내 정보 조회
        mockMvc.perform(get("/v1/auth/me")
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("loginUser"));
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 미인증 (401)")
    void getMyInfo_fail_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/auth/me"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
