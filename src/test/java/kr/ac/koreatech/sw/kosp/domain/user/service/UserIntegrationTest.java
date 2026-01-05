package kr.ac.koreatech.sw.kosp.domain.user.service;


import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class UserIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User targetUser;
    private String accessToken;

    @BeforeEach
    void setup() throws Exception {
        createRole("ROLE_STUDENT");
        createRole("ROLE_ADMIN");
        createGithubUser(222L);
        createGithubUser(333L);
        createGithubUser(444L);

        // 1. Signup
        String signupToken = createSignupToken(222L, "target@koreatech.ac.kr");
        UserSignupRequest signupReq = new UserSignupRequest(
            "targetUser", "2020136222", "target@koreatech.ac.kr", getValidPassword(), signupToken
        );
        userService.signup(signupReq);
        targetUser = userRepository.findByKutEmail("target@koreatech.ac.kr").orElseThrow();

        // 2. Login to get JWT tokens
        LoginRequest loginReq = new LoginRequest("target@koreatech.ac.kr", getValidPassword());
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andReturn();
        
        String response = result.getResponse().getContentAsString();
        kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse tokens = 
            objectMapper.readValue(response, kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse.class);
        accessToken = tokens.accessToken();
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    void update_myInfo_success() throws Exception {
        // given
        UserUpdateRequest updateReq = new UserUpdateRequest("updatedName", "updatedIntro");

        // when
        mockMvc.perform(put("/v1/users/" + targetUser.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isOk());

        // then
        User updated = userRepository.findById(targetUser.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("updatedName");
        assertThat(updated.getIntroduction()).isEqualTo("updatedIntro");
    }

    @Test
    @DisplayName("정보 수정 실패 - 권한 없음 (타인 계정)")
    void update_otherInfo_fail() throws Exception {
        // given: Signup another user
        String otherToken = createSignupToken(333L, "other@koreatech.ac.kr");
        UserSignupRequest otherReq = new UserSignupRequest(
            "otherUser", "2020136333", "other@koreatech.ac.kr", "pw", otherToken
        );
        userService.signup(otherReq);
        User otherUser = userRepository.findByKutEmail("other@koreatech.ac.kr").orElseThrow();

        UserUpdateRequest updateReq = new UserUpdateRequest("hacker", "hacking");

        // when & then: Try to update otherUser with targetUser's session
        mockMvc.perform(put("/v1/users/" + otherUser.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void delete_myAccount_success() throws Exception {
        // when
        mockMvc.perform(delete("/v1/users/" + targetUser.getId())
                .header("Authorization", "Bearer " + accessToken))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        User deleted = userRepository.findById(targetUser.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 타인 계정 탈퇴 시도")
    void delete_otherAccount_fail() throws Exception {
        // given
        String victimToken = createSignupToken(444L, "victim@koreatech.ac.kr");
        UserSignupRequest otherReq = new UserSignupRequest(
            "victim", "2020136444", "victim@koreatech.ac.kr", "pw", victimToken
        );
        userService.signup(otherReq);
        User victim = userRepository.findByKutEmail("victim@koreatech.ac.kr").orElseThrow();

        // when & then
        mockMvc.perform(delete("/v1/users/" + victim.getId())
                .header("Authorization", "Bearer " + accessToken))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getProfile_success() throws Exception {
        mockMvc.perform(get("/v1/users/" + targetUser.getId())
                .header("Authorization", "Bearer " + accessToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("targetUser"));
    }
}
