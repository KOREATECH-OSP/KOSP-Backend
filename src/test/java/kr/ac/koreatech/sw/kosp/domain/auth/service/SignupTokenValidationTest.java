package kr.ac.koreatech.sw.kosp.domain.auth.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;

class SignupTokenValidationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("유효한 회원가입 토큰 검증 성공")
    void validateSignupToken_success() throws Exception {
        // given
        createGithubUser(100L);
        String validToken = createSignupToken(100L, "test@koreatech.ac.kr");

        // when & then
        mockMvc.perform(get("/v1/auth/verify/token/signup")
                .param("token", validToken))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잘못된 서명의 회원가입 토큰 검증 실패")
    void validateSignupToken_invalidSignature() throws Exception {
        // given
        String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDAiLCJjYXRlZ29yeSI6InNpZ251cCJ9.INVALID_SIGNATURE";

        // when & then
        mockMvc.perform(get("/v1/auth/verify/token/signup")
                .param("token", tamperedToken))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("빈 회원가입 토큰 검증 실패")
    void validateSignupToken_emptyToken() throws Exception {
        // when & then
        mockMvc.perform(get("/v1/auth/verify/token/signup")
                .param("token", ""))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
