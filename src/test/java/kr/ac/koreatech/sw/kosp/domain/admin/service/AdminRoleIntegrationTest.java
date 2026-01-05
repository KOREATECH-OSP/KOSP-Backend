package kr.ac.koreatech.sw.kosp.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PolicyRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;

class AdminRoleIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PolicyRepository policyRepository;

    private String adminAccessToken;

    @BeforeEach
    void setup() throws Exception {
        createGithubUser(500L);

        // Create Admin
        String signupToken = createSignupToken(500L, "admin.role@koreatech.ac.kr");
        UserSignupRequest adminReq = new UserSignupRequest(
            "adminRole", "2020000500", "admin.role@koreatech.ac.kr", getValidPassword(), signupToken
        );
        userService.signup(adminReq);
        User admin = userRepository.findByKutEmail("admin.role@koreatech.ac.kr").orElseThrow();
        kr.ac.koreatech.sw.kosp.domain.auth.model.Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        // Login Admin
        adminAccessToken = loginAndGetToken("admin.role@koreatech.ac.kr", getValidPassword());
    }

    // ========== Role Tests ==========

    @Test
    @DisplayName("역할 목록 조회 성공")
    void getAllRoles_success() throws Exception {
        mockMvc.perform(get("/v1/admin/roles")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.name == 'ROLE_ADMIN')]").exists());
    }

    @Test
    @DisplayName("역할 단일 조회 성공")
    void getRole_success() throws Exception {
        mockMvc.perform(get("/v1/admin/roles/ROLE_ADMIN")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("ROLE_ADMIN"))
            .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @DisplayName("역할 생성 성공")
    void createRole_success() throws Exception {
        // given
        RoleRequest req = new RoleRequest("ROLE_MANAGER", "매니저");

        // when
        mockMvc.perform(post("/v1/admin/roles")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        // then
        assertThat(roleRepository.findByName("ROLE_MANAGER")).isPresent();
    }

    @Test
    @DisplayName("역할 수정 성공")
    void updateRole_success() throws Exception {
        // given
        RoleRequest createReq = new RoleRequest("ROLE_EDITOR", "편집자");
        mockMvc.perform(post("/v1/admin/roles")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated());

        RoleUpdateRequest updateReq = new RoleUpdateRequest("수정된 편집자 설명");

        // when
        mockMvc.perform(put("/v1/admin/roles/ROLE_EDITOR")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isOk());

        // then
        kr.ac.koreatech.sw.kosp.domain.auth.model.Role role = roleRepository.findByName("ROLE_EDITOR").orElseThrow();
        assertThat(role.getDescription()).isEqualTo("수정된 편집자 설명");
    }

    @Test
    @DisplayName("역할 삭제 성공")
    void deleteRole_success() throws Exception {
        // given
        RoleRequest createReq = new RoleRequest("ROLE_TEMP", "임시 역할");
        mockMvc.perform(post("/v1/admin/roles")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated());

        // when
        mockMvc.perform(delete("/v1/admin/roles/ROLE_TEMP")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        assertThat(roleRepository.findByName("ROLE_TEMP")).isEmpty();
    }

    @Test
    @DisplayName("역할에서 정책 제거 성공")
    void removePolicy_success() throws Exception {
        // given - Create role and policy
        RoleRequest roleReq = new RoleRequest("ROLE_TEST", "테스트 역할");
        mockMvc.perform(post("/v1/admin/roles")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleReq)))
            .andExpect(status().isCreated());

        PolicyCreateRequest policyReq = new PolicyCreateRequest("TEST_POLICY", "테스트 정책");
        mockMvc.perform(post("/v1/admin/policies")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyReq)))
            .andExpect(status().isCreated());

        // Assign policy to role
        mockMvc.perform(post("/v1/admin/roles/ROLE_TEST/policies")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"policyName\":\"TEST_POLICY\"}"))
            .andExpect(status().isOk());

        // when - Remove policy from role
        mockMvc.perform(delete("/v1/admin/roles/ROLE_TEST/policies/TEST_POLICY")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        kr.ac.koreatech.sw.kosp.domain.auth.model.Role role = roleRepository.findByName("ROLE_TEST").orElseThrow();
        assertThat(role.getPolicies()).isEmpty();
    }

    // ========== Policy Tests ==========

    @Test
    @DisplayName("정책 목록 조회 성공")
    void getAllPolicies_success() throws Exception {
        mockMvc.perform(get("/v1/admin/policies")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("정책 단일 조회 성공")
    void getPolicy_success() throws Exception {
        // given
        PolicyCreateRequest req = new PolicyCreateRequest("READ_POLICY", "읽기 정책");
        mockMvc.perform(post("/v1/admin/policies")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/v1/admin/policies/READ_POLICY")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("READ_POLICY"))
            .andExpect(jsonPath("$.description").value("읽기 정책"));
    }

    @Test
    @DisplayName("정책 생성 성공")
    void createPolicy_success() throws Exception {
        // given
        PolicyCreateRequest req = new PolicyCreateRequest("NEW_POLICY", "새 정책");

        // when
        mockMvc.perform(post("/v1/admin/policies")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        // then
        assertThat(policyRepository.findByName("NEW_POLICY")).isPresent();
    }

    @Test
    @DisplayName("정책 수정 성공")
    void updatePolicy_success() throws Exception {
        // given
        PolicyCreateRequest createReq = new PolicyCreateRequest("WRITE_POLICY", "쓰기 정책");
        mockMvc.perform(post("/v1/admin/policies")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated());

        PolicyUpdateRequest updateReq = new PolicyUpdateRequest("수정된 쓰기 정책");

        // when
        mockMvc.perform(put("/v1/admin/policies/WRITE_POLICY")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isOk());

        // then
        Policy policy = policyRepository.findByName("WRITE_POLICY").orElseThrow();
        assertThat(policy.getDescription()).isEqualTo("수정된 쓰기 정책");
    }

    @Test
    @DisplayName("정책 삭제 성공")
    void deletePolicy_success() throws Exception {
        // given
        PolicyCreateRequest createReq = new PolicyCreateRequest("DELETE_POLICY", "삭제될 정책");
        mockMvc.perform(post("/v1/admin/policies")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated());

        // when
        mockMvc.perform(delete("/v1/admin/policies/DELETE_POLICY")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        assertThat(policyRepository.findByName("DELETE_POLICY")).isEmpty();
    }

    @Test
    @DisplayName("정책에서 권한 제거 성공")
    void removePermission_success() throws Exception {
        // given - Create policy
        PolicyCreateRequest policyReq = new PolicyCreateRequest("MANAGE_POLICY", "관리 정책");
        mockMvc.perform(post("/v1/admin/policies")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyReq)))
            .andExpect(status().isCreated());

        // Assign permission to policy
        mockMvc.perform(post("/v1/admin/policies/MANAGE_POLICY/permissions")
                .header("Authorization", bearerToken(adminAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionName\":\"admin:roles:read\"}"))
            .andExpect(status().isOk());

        // when - Remove permission from policy
        mockMvc.perform(delete("/v1/admin/policies/MANAGE_POLICY/permissions/admin:roles:read")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        Policy policy = policyRepository.findByName("MANAGE_POLICY").orElseThrow();
        assertThat(policy.getPermissions()).noneMatch(p -> p.getName().equals("admin:roles:read"));
    }

    // ========== Permission Tests (READ ONLY) ==========

    @Test
    @DisplayName("권한 목록 조회 성공")
    void getAllPermissions_success() throws Exception {
        mockMvc.perform(get("/v1/admin/permissions")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("권한 단일 조회 성공")
    void getPermission_success() throws Exception {
        mockMvc.perform(get("/v1/admin/permissions/admin:roles:read")
                .header("Authorization", bearerToken(adminAccessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("admin:roles:read"))
            .andExpect(jsonPath("$.description").exists());
    }
}
