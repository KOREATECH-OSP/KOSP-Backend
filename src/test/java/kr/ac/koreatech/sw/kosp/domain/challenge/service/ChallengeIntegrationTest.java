package kr.ac.koreatech.sw.kosp.domain.challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChallengeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    private String accessToken;

    @BeforeEach
    void setup() throws Exception {
        createGithubUser(777L);

        // Signup & Login
        String signupToken = createSignupToken(777L, "challenger@koreatech.ac.kr");
        UserSignupRequest signupReq = new UserSignupRequest(
            "challenger", "2020136777", "challenger@koreatech.ac.kr", getValidPassword(), signupToken
        );
        userService.signup(signupReq);
        accessToken = loginAndGetToken("challenger@koreatech.ac.kr", getValidPassword());
        
        // Note: Challenges should be initialized by Initializer or Admin.
        // Assuming empty list or testing empty state if not initialized.
        // If Initializer runs, we might expect some default challenges.
        // For now, simple 200 OK check is enough for CLG-002 integration.
    }

    @Test
    @DisplayName("챌린지 목록 조회 성공")
    void getChallenges_success() throws Exception {
        mockMvc.perform(get("/v1/challenges")
                .header("Authorization", bearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.challenges").isArray());
    }
}
