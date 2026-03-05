package io.swkoreatech.kosp.domain.community.recruit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.domain.auth.repository.PermissionRepository;
import io.swkoreatech.kosp.domain.auth.repository.PolicyRepository;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.board.repository.BoardRepository;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyDecisionRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyRequest;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.user.dto.response.MyApplicationListResponse;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.global.common.IntegrationTestSupport;

@DisplayName("RecruitController 통합 테스트")
class RecruitControllerTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private RecruitApplyRepository recruitApplyRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private User leader;
    private User applicant;
    private Board board;
    private Team team;
    private Recruit recruit;
    private String leaderToken;
    private String applicantToken;

    @BeforeEach
    void setUp() throws Exception {
        createGithubUser(1001L);
        createGithubUser(1002L);

        GithubUser leaderGithubUser = githubUserRepository.getByGithubId(1001L);
        GithubUser applicantGithubUser = githubUserRepository.getByGithubId(1002L);

        leader = User.builder()
            .name("팀장")
            .kutId("2024001")
            .kutEmail("leader@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .githubUser(leaderGithubUser)
            .build();
        leader = userRepository.save(leader);

        // Assign ROLE_STUDENT to leader (includes recruit:applications:decide permission)
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
            .orElseThrow(() -> new RuntimeException("ROLE_STUDENT not found - PermissionInitializer didn't run"));
        leader.getRoles().add(studentRole);
        leader = userRepository.save(leader);

        // Ensure StudentPolicy includes recruit:applications:decide permission
        Permission decidePermission = permissionRepository.getByName("recruit:applications:decide");
        Policy studentPolicy = policyRepository.getByName("StudentPolicy");
        Set<Permission> updatedPermissions = new HashSet<>(studentPolicy.getPermissions());
        updatedPermissions.add(decidePermission);
        studentPolicy.updatePermissions(updatedPermissions);
        policyRepository.save(studentPolicy);

        applicant = User.builder()
            .name("지원자")
            .kutId("2024002")
            .kutEmail("applicant@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .githubUser(applicantGithubUser)
            .build();
        applicant = userRepository.save(applicant);

        board = Board.builder()
            .name("모집게시판")
            .description("팀원 모집 게시판")
            .build();
        ReflectionTestUtils.setField(board, "isRecruitAllowed", true);
        board = boardRepository.save(board);

        team = Team.builder()
            .name("테스트팀")
            .description("통합 테스트 팀")
            .build();
        team = teamRepository.save(team);

        TeamMember leaderMember = TeamMember.builder()
            .team(team)
            .user(leader)
            .role(TeamRole.LEADER)
            .build();
        teamMemberRepository.save(leaderMember);

        recruit = Recruit.recruitBuilder()
            .author(leader)
            .board(board)
            .title("팀원 모집합니다")
            .content("열정적인 팀원을 찾습니다")
            .team(team)
            .status(RecruitStatus.OPEN)
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(7))
            .build();
        recruit = recruitRepository.save(recruit);

        leaderToken = createAccessToken(leader);
        applicantToken = createAccessToken(applicant);
    }

    @Test
    @DisplayName("지원 결정 성공")
    void decideApplication_성공() throws Exception {
        RecruitApply apply = RecruitApply.builder()
            .recruit(recruit)
            .user(applicant)
            .reason("열심히 하겠습니다")
            .portfolioUrl("https://github.com/applicant")
            .build();
        apply = recruitApplyRepository.save(apply);

        String decisionReason = "지원자의 GitHub 활동이 우수하여 수락합니다.";
        RecruitApplyDecisionRequest request = new RecruitApplyDecisionRequest(
            ApplyStatus.ACCEPTED,
            decisionReason
        );

        mockMvc.perform(patch("/v1/community/recruits/applications/{applicationId}", apply.getId())
                .header("Authorization", "Bearer " + leaderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        RecruitApply updated = recruitApplyRepository.findById(apply.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ApplyStatus.ACCEPTED);
        assertThat(updated.getDecisionReason()).isEqualTo(decisionReason);
    }

    @Test
    @DisplayName("사유 500자 초과 시 400 에러")
    void decideApplication_사유500자초과_400에러() throws Exception {
        RecruitApply apply = RecruitApply.builder()
            .recruit(recruit)
            .user(applicant)
            .reason("열심히 하겠습니다")
            .portfolioUrl("https://github.com/applicant")
            .build();
        apply = recruitApplyRepository.save(apply);

        String longReason = "a".repeat(501);
        RecruitApplyDecisionRequest request = new RecruitApplyDecisionRequest(
            ApplyStatus.REJECTED,
            longReason
        );

        mockMvc.perform(patch("/v1/community/recruits/applications/{applicationId}", apply.getId())
                .header("Authorization", "Bearer " + leaderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("본인 지원 내역 조회 시 decisionReason 포함")
    void getMyApplications_decisionReason포함() throws Exception {
        RecruitApply apply = RecruitApply.builder()
            .recruit(recruit)
            .user(applicant)
            .reason("참여하고 싶습니다")
            .portfolioUrl("https://github.com/applicant")
            .build();
        apply = recruitApplyRepository.save(apply);

        apply.updateStatus(ApplyStatus.ACCEPTED);
        String expectedReason = "GitHub 활동이 매우 활발하여 수락합니다.";
        apply.updateDecisionReason(expectedReason);
        recruitApplyRepository.save(apply);

        MvcResult result = mockMvc.perform(get("/v1/users/me/applications")
                .header("Authorization", "Bearer " + applicantToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applications").isArray())
            .andExpect(jsonPath("$.applications[0].decisionReason").value(expectedReason))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        MyApplicationListResponse response = objectMapper.readValue(
            responseBody,
            MyApplicationListResponse.class
        );

        assertThat(response.applications()).hasSize(1);
        assertThat(response.applications().get(0).decisionReason()).isEqualTo(expectedReason);
    }

    @Test
    @DisplayName("비로그인 사용자는 canApply가 false")
    @Sql("classpath:data/recruit-can-apply-test.sql")
    void getList_anonymousUser_returnsCanApplyFalse() throws Exception {
        mockMvc.perform(get("/v1/community/recruits")
                .param("boardId", "1")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recruits[0].canApply").value(false));
    }

    @Test
    @DisplayName("이미 지원한 사용자는 canApply가 false")
    @Sql("classpath:data/recruit-can-apply-test.sql")
    void getList_userAlreadyApplied_returnsCanApplyFalse() throws Exception {
        User userHasApplied = userRepository.findByKutEmail("user-has-applied@koreatech.ac.kr")
            .orElseThrow();
        String token = createAccessToken(userHasApplied);

        mockMvc.perform(get("/v1/community/recruits")
                .header("Authorization", "Bearer " + token)
                .param("boardId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recruits[?(@.id==200)].canApply").value(false));
    }

    @Test
    @DisplayName("지원 가능한 사용자는 canApply가 true")
    @Sql("classpath:data/recruit-can-apply-test.sql")
    void getList_eligibleUser_returnsCanApplyTrue() throws Exception {
        User userCanApply = userRepository.findByKutEmail("user-can-apply@koreatech.ac.kr")
            .orElseThrow();
        String token = createAccessToken(userCanApply);

        mockMvc.perform(get("/v1/community/recruits")
                .header("Authorization", "Bearer " + token)
                .param("boardId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recruits[?(@.id==200)].canApply").value(true));
    }

    @Test
    @DisplayName("잘못된 RSQL 문법은 400 에러")
    void getList_malformedRsql_returns400() throws Exception {
        mockMvc.perform(get("/v1/community/recruits")
                .param("boardId", String.valueOf(board.getId()))
                .param("rsql", "(title=="))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("페이지 크기는 100으로 제한")
    void getList_largePageSize_cappedAt100() throws Exception {
        mockMvc.perform(get("/v1/community/recruits")
                .param("boardId", String.valueOf(board.getId()))
                .param("size", "999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagination.totalPages").isNumber());
    }

    @Test
    @DisplayName("삭제된 모집공고는 isDeleted가 true")
    @Sql("classpath:data/recruit-can-apply-test.sql")
    void getList_deletedRecruit_returnsIsDeletedTrue() throws Exception {
        mockMvc.perform(get("/v1/community/recruits")
                .param("boardId", "1")
                .param("rsql", "id==201"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recruits[0].isDeleted").value(true));
    }
}
