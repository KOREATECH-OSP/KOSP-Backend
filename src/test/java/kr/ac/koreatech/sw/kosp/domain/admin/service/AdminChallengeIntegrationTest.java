package kr.ac.koreatech.sw.kosp.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;
import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;

class AdminChallengeIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ChallengeRepository challengeRepository;

    private String adminAccessToken;

    @BeforeEach
    void setup() throws Exception {
        createGithubUser(101L);

        // Create Admin
        String signupToken = createSignupToken(101L, "admin.ch@koreatech.ac.kr");
        UserSignupRequest adminReq = new UserSignupRequest(
            "adminCh", "2020000001", "admin.ch@koreatech.ac.kr", getValidPassword(), signupToken
        );
        userService.signup(adminReq);
        User admin = userRepository.findByKutEmail("admin.ch@koreatech.ac.kr").orElseThrow();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        // Login Admin
        adminAccessToken = loginAndGetToken("admin.ch@koreatech.ac.kr", getValidPassword());
    }

    @Test
    @DisplayName("관리자 - 챌린지 생성")
    void createChallenge_success() throws Exception {
        // given
        kr.ac.koreatech.sw.kosp.domain.challenge.dto.request.ChallengeRequest req = new kr.ac.koreatech.sw.kosp.domain.challenge.dto.request.ChallengeRequest(
            "New Challenge", "Description", "evaluationLogic > 10", 100, "http://image.url", 50, 100, "activity.totalCommits"
        );

        // when
        mockMvc.perform(post("/v1/admin/challenges")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated()); // or Created

        // then
        List<Challenge> challenges = challengeRepository.findAll();
        assertThat(challenges).extracting("name").contains("New Challenge");
    }

    @Test
    @DisplayName("관리자 - 챌린지 목록 조회")
    void getChallenges_success() throws Exception {
        // given: 챌린지 2개 생성
        Challenge challenge1 = Challenge.builder()
            .name("Challenge 1")
            .description("First Challenge")
            .condition("score > 100")
            .tier(1)
            .imageUrl("http://image1.url")
            .point(100)
            .maxProgress(100)
            .progressField("activity.totalCommits")
            .build();
        Challenge challenge2 = Challenge.builder()
            .name("Challenge 2")
            .description("Second Challenge")
            .condition("commits > 50")
            .tier(2)
            .imageUrl("http://image2.url")
            .point(200)
            .maxProgress(50)
            .progressField("activity.commits")
            .build();
        challengeRepository.save(challenge1);
        challengeRepository.save(challenge2);

        // when & then
        mockMvc.perform(get("/v1/admin/challenges")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.challenges").isArray())
            .andExpect(jsonPath("$.challenges.length()").value(2))
            .andExpect(jsonPath("$.challenges[0].name").exists())
            .andExpect(jsonPath("$.challenges[0].condition").exists());
    }
}
