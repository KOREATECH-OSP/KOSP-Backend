package kr.ac.koreatech.sw.kosp.domain.community.recruit.service;

import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class RecruitIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private RecruitRepository recruitRepository;
    @Autowired
    private kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository boardRepository;
    @Autowired
    private kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamRepository teamRepository;

    private MockHttpSession session;
    private MockHttpSession otherUserSession;
    private kr.ac.koreatech.sw.kosp.domain.community.board.model.Board testBoard;
    private kr.ac.koreatech.sw.kosp.domain.community.team.model.Team testTeam;

    @BeforeEach
    void setup() throws Exception {
        createRole("ROLE_STUDENT");
        createGithubUser(999L); // used in signup

        // Signup & Login
        String recruiterToken = createSignupToken(999L, "recruit@koreatech.ac.kr");
        userService.signup(new UserSignupRequest(
            "recruiter", "2020136999", "recruit@koreatech.ac.kr", getValidPassword(), recruiterToken
        ));
        LoginRequest loginReq = new LoginRequest("recruit@koreatech.ac.kr", getValidPassword());
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andReturn();
        session = (MockHttpSession) result.getRequest().getSession();

        // Create another user for testing
        createGithubUser(998L);
        String otherToken = createSignupToken(998L, "other@koreatech.ac.kr");
        userService.signup(new UserSignupRequest(
            "otherUser", "2020136998", "other@koreatech.ac.kr", getValidPassword(), otherToken
        ));
        LoginRequest otherLogin = new LoginRequest("other@koreatech.ac.kr", getValidPassword());
        MvcResult otherResult = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherLogin)))
            .andExpect(status().isOk())
            .andReturn();
        otherUserSession = (MockHttpSession) otherResult.getRequest().getSession();

        // Create test board and team
        testBoard = kr.ac.koreatech.sw.kosp.domain.community.board.model.Board.builder()
            .name("Recruit Board").description("Desc").isRecruitAllowed(true).build();
        boardRepository.save(testBoard);

        testTeam = kr.ac.koreatech.sw.kosp.domain.community.team.model.Team.builder()
            .name("Team A").description("Desc").build();
        teamRepository.save(testTeam);
    }

    @Test
    @DisplayName("모집 공고 작성 성공")
    void createRecruit_success() throws Exception {
        // given
        kr.ac.koreatech.sw.kosp.domain.community.board.model.Board board = kr.ac.koreatech.sw.kosp.domain.community.board.model.Board.builder()
            .name("Recruit Board").description("Desc").isRecruitAllowed(true).build();
        boardRepository.save(board);

        kr.ac.koreatech.sw.kosp.domain.community.team.model.Team team = kr.ac.koreatech.sw.kosp.domain.community.team.model.Team.builder()
            .name("Team A").description("Desc").build();
        teamRepository.save(team);

        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest req = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            board.getId(), "Recruit Title", "Content", List.of("Java"), team.getId(),
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7)
        );

        // when
        mockMvc.perform(post("/v1/community/recruits")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        // then
        Recruit recruit = recruitRepository.findByBoard(board, org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);
        assertThat(recruit.getTitle()).isEqualTo("Recruit Title");
    }

    @Test
    @DisplayName("모집 공고 상세 조회 성공")
    void getRecruit_success() throws Exception {
        // given: 공고 생성
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest req = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "Recruit Title", "Content", List.of("Java"), testTeam.getId(),
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7)
        );
        mockMvc.perform(post("/v1/community/recruits")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Recruit recruit = recruitRepository.findByBoard(testBoard, org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when & then
        mockMvc.perform(get("/v1/community/recruits/" + recruit.getId())
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Recruit Title"))
            .andExpect(jsonPath("$.content").value("Content"));
    }

    @Test
    @DisplayName("모집 공고 목록 조회 성공")
    void getRecruitList_success() throws Exception {
        // given: 공고 2개 생성
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest req1 = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "Recruit 1", "Content 1", List.of("Java"), testTeam.getId(),
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7)
        );
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest req2 = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "Recruit 2", "Content 2", List.of("Python"), testTeam.getId(),
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7)
        );
        mockMvc.perform(post("/v1/community/recruits")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/community/recruits")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/v1/community/recruits?boardId=" + testBoard.getId())
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recruits").isArray())
            .andExpect(jsonPath("$.recruits.length()").value(2));
    }

    @Test
    @DisplayName("모집 공고 수정 성공")
    void updateRecruit_success() throws Exception {
        // given: 공고 생성
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest createReq = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "Original Title", "Original Content", List.of("Java"), testTeam.getId(),
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7)
        );
        mockMvc.perform(post("/v1/community/recruits")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated());

        Recruit recruit = recruitRepository.findByBoard(testBoard, org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when: 수정
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest updateReq = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "Updated Title", "Updated Content", List.of("Python"), testTeam.getId(),
            LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(8)
        );
        mockMvc.perform(put("/v1/community/recruits/" + recruit.getId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isOk());

        // then
        Recruit updated = recruitRepository.findById(recruit.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getContent()).isEqualTo("Updated Content");
    }

    @Test
    @DisplayName("모집 공고 수정 실패 - 타인 공고")
    void updateRecruit_fail_notOwner() throws Exception {
        // given: recruiter가 공고 생성
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest createReq = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "Original Title", "Original Content", List.of("Java"), testTeam.getId(),
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7)
        );
        mockMvc.perform(post("/v1/community/recruits")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated());

        Recruit recruit = recruitRepository.findByBoard(testBoard, org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when & then: otherUser가 수정 시도
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest updateReq = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "Updated Title", "Updated Content", List.of("Python"), testTeam.getId(),
            LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(8)
        );
        mockMvc.perform(put("/v1/community/recruits/" + recruit.getId())
                .session(otherUserSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("모집 공고 삭제 성공")
    void deleteRecruit_success() throws Exception {
        // given: 공고 생성
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest createReq = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "To Delete", "Content", List.of("Java"), testTeam.getId(),
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7)
        );
        mockMvc.perform(post("/v1/community/recruits")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated());

        Recruit recruit = recruitRepository.findByBoard(testBoard, org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when
        mockMvc.perform(delete("/v1/community/recruits/" + recruit.getId())
                .session(session))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        assertThat(recruitRepository.findById(recruit.getId())).isEmpty();
    }

    @Test
    @DisplayName("모집 공고 삭제 실패 - 타인 공고")
    void deleteRecruit_fail_notOwner() throws Exception {
        // given: recruiter가 공고 생성
        kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest createReq = new kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest(
            testBoard.getId(), "To Delete", "Content", List.of("Java"), testTeam.getId(),
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7)
        );
        mockMvc.perform(post("/v1/community/recruits")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated());

        Recruit recruit = recruitRepository.findByBoard(testBoard, org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when & then: otherUser가 삭제 시도
        mockMvc.perform(delete("/v1/community/recruits/" + recruit.getId())
                .session(otherUserSession))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
}
