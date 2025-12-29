package kr.ac.koreatech.sw.kosp.domain.admin.service;

import jakarta.servlet.http.HttpSession;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;

import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;
import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

class AdminChallengeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ChallengeRepository challengeRepository;

    private MockHttpSession adminSession;

    @BeforeEach
    void setup() throws Exception {
        createRole("ROLE_ADMIN");
        createGithubUser(101L);

        // Create Admin
        UserSignupRequest adminReq = new UserSignupRequest(
            "adminCh", "2020000001", "admin.ch@koreatech.ac.kr", getValidPassword(), 101L
        );
        userService.signup(adminReq);
        User admin = userRepository.findByKutEmail("admin.ch@koreatech.ac.kr").orElseThrow();
        kr.ac.koreatech.sw.kosp.domain.auth.model.Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        // Login Admin
        LoginRequest loginAdmin = new LoginRequest("admin.ch@koreatech.ac.kr", getValidPassword());
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginAdmin))).andReturn();
        adminSession = (MockHttpSession) result.getRequest().getSession();
    }

    @Test
    @DisplayName("관리자 - 챌린지 생성")
    void createChallenge_success() throws Exception {
        // given
        kr.ac.koreatech.sw.kosp.domain.challenge.dto.request.ChallengeRequest req = new kr.ac.koreatech.sw.kosp.domain.challenge.dto.request.ChallengeRequest(
            "New Challenge", "Description", "evaluationLogic > 10", 100, "http://image.url"
        );

        // when
        mockMvc.perform(post("/v1/admin/challenges")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated()); // or Created

        // then
        List<Challenge> challenges = challengeRepository.findAll();
        assertThat(challenges).extracting("name").contains("New Challenge");
    }
}
