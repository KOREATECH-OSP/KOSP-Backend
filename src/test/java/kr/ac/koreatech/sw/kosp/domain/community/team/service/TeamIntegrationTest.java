package kr.ac.koreatech.sw.kosp.domain.community.team.service;

import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TeamIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private TeamRepository teamRepository;

    private MockHttpSession session;

    @BeforeEach
    void setup() throws Exception {
        createRole("ROLE_STUDENT");
        createGithubUser(1001L);

        // Signup & Login
        userService.signup(new UserSignupRequest(
            "teamLeader", "2020001001", "leader@koreatech.ac.kr", getValidPassword(), 1001L
        ));
        LoginRequest loginReq = new LoginRequest("leader@koreatech.ac.kr", getValidPassword());
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andReturn();
        session = (MockHttpSession) result.getRequest().getSession();
    }

    @Test
    @DisplayName("팀 생성 성공")
    void createTeam_success() throws Exception {
        // given
        TeamCreateRequest req = new TeamCreateRequest(
            "KOSP Team", "오픈소스 프로젝트 팀", "https://example.com/image.png"
        );

        // when
        mockMvc.perform(post("/v1/teams")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        // then
        Team team = teamRepository.findByNameContaining("KOSP", org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);
        assertThat(team.getName()).isEqualTo("KOSP Team");
        assertThat(team.getDescription()).isEqualTo("오픈소스 프로젝트 팀");
    }

    @Test
    @DisplayName("팀 상세 조회 성공")
    void getTeam_success() throws Exception {
        // given: 팀 생성
        TeamCreateRequest req = new TeamCreateRequest(
            "Test Team", "Test Description", "https://example.com/image.png"
        );
        mockMvc.perform(post("/v1/teams")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Team team = teamRepository.findByNameContaining("Test", org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when & then
        mockMvc.perform(get("/v1/teams/" + team.getId())
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Team"))
            .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("팀 목록 조회 성공")
    void getTeamList_success() throws Exception {
        // given: 팀 2개 생성
        TeamCreateRequest req1 = new TeamCreateRequest(
            "Team Alpha", "First Team", "https://example.com/alpha.png"
        );
        TeamCreateRequest req2 = new TeamCreateRequest(
            "Team Beta", "Second Team", "https://example.com/beta.png"
        );
        mockMvc.perform(post("/v1/teams")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/teams")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/v1/teams?search=Team")
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams").isArray())
            .andExpect(jsonPath("$.teams.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("팀 목록 조회 - 검색")
    void getTeamList_withSearch() throws Exception {
        // given: 팀 생성
        TeamCreateRequest req1 = new TeamCreateRequest(
            "KOSP Team", "Open Source", "https://example.com/kosp.png"
        );
        TeamCreateRequest req2 = new TeamCreateRequest(
            "Other Team", "Different Team", "https://example.com/other.png"
        );
        mockMvc.perform(post("/v1/teams")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/teams")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isCreated());

        // when & then: KOSP 검색
        mockMvc.perform(get("/v1/teams?search=KOSP")
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams").isArray())
            .andExpect(jsonPath("$.teams.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.teams[?(@.name == 'KOSP Team')]").exists());
    }
}
