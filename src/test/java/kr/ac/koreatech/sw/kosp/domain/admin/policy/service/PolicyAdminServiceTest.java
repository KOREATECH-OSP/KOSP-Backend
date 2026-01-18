package kr.ac.koreatech.sw.kosp.domain.admin.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PermissionResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PolicyResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PermissionRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PolicyRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyAdminService 단위 테스트")
class PolicyAdminServiceTest {

    @InjectMocks
    private PolicyAdminService policyAdminService;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PermissionRepository permissionRepository;

    private Policy createPolicy(Long id, String name) {
        Policy policy = Policy.builder()
            .name(name)
            .description(name + " 정책")
            .permissions(new HashSet<>())
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(policy, "id", id);
        return policy;
    }

    private Permission createPermission(Long id, String name) {
        Permission permission = Permission.builder()
            .name(name)
            .description(name + " 권한")
            .build();
        ReflectionTestUtils.setField(permission, "id", id);
        return permission;
    }

    private Role createRole(Long id, String name) {
        Role role = Role.builder()
            .name(name)
            .description(name + " 역할")
            .build();
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    @Nested
    @DisplayName("getAllPolicies 메서드")
    class GetAllPoliciesTest {

        @Test
        @DisplayName("모든 정책 목록을 반환한다")
        void returnsAllPolicies() {
            // given
            Policy policy1 = createPolicy(1L, "POLICY_READ");
            Policy policy2 = createPolicy(2L, "POLICY_WRITE");
            given(policyRepository.findAll()).willReturn(List.of(policy1, policy2));

            // when
            List<PolicyResponse> result = policyAdminService.getAllPolicies();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("정책이 없으면 빈 리스트를 반환한다")
        void returnsEmptyList_whenNoPolicies() {
            // given
            given(policyRepository.findAll()).willReturn(List.of());

            // when
            List<PolicyResponse> result = policyAdminService.getAllPolicies();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPolicy 메서드")
    class GetPolicyTest {

        @Test
        @DisplayName("존재하는 정책을 조회한다")
        void returnsPolicy_whenExists() {
            // given
            Policy policy = createPolicy(1L, "POLICY_READ");
            given(policyRepository.findByName("POLICY_READ")).willReturn(Optional.of(policy));

            // when
            PolicyResponse result = policyAdminService.getPolicy("POLICY_READ");

            // then
            assertThat(result.name()).isEqualTo("POLICY_READ");
        }

        @Test
        @DisplayName("존재하지 않는 정책을 조회하면 예외가 발생한다")
        void throwsException_whenPolicyNotFound() {
            // given
            given(policyRepository.findByName("INVALID")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> policyAdminService.getPolicy("INVALID"))
                .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("createPolicy 메서드")
    class CreatePolicyTest {

        @Test
        @DisplayName("중복된 이름의 정책을 생성하면 예외가 발생한다")
        void throwsException_whenDuplicateName() {
            // given
            Policy existing = createPolicy(1L, "POLICY_EXISTING");
            given(policyRepository.findByName("POLICY_EXISTING")).willReturn(Optional.of(existing));
            PolicyCreateRequest request = new PolicyCreateRequest("POLICY_EXISTING", "기존 정책");

            // when & then
            assertThatThrownBy(() -> policyAdminService.createPolicy(request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("새 정책을 성공적으로 생성한다")
        void createsPolicySuccessfully() {
            // given
            given(policyRepository.findByName("POLICY_NEW")).willReturn(Optional.empty());
            PolicyCreateRequest request = new PolicyCreateRequest("POLICY_NEW", "새 정책");

            // when
            policyAdminService.createPolicy(request);

            // then
            verify(policyRepository).save(any(Policy.class));
        }
    }

    @Nested
    @DisplayName("updatePolicy 메서드")
    class UpdatePolicyTest {

        @Test
        @DisplayName("존재하지 않는 정책을 수정하면 예외가 발생한다")
        void throwsException_whenPolicyNotFound() {
            // given
            given(policyRepository.findByName("INVALID")).willReturn(Optional.empty());
            PolicyUpdateRequest request = new PolicyUpdateRequest("수정된 설명");

            // when & then
            assertThatThrownBy(() -> policyAdminService.updatePolicy("INVALID", request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("정책 설명을 성공적으로 수정한다")
        void updatesDescriptionSuccessfully() {
            // given
            Policy policy = createPolicy(1L, "POLICY_READ");
            given(policyRepository.findByName("POLICY_READ")).willReturn(Optional.of(policy));
            PolicyUpdateRequest request = new PolicyUpdateRequest("수정된 설명");

            // when
            policyAdminService.updatePolicy("POLICY_READ", request);

            // then
            assertThat(policy.getDescription()).isEqualTo("수정된 설명");
        }
    }

    @Nested
    @DisplayName("deletePolicy 메서드")
    class DeletePolicyTest {

        @Test
        @DisplayName("존재하지 않는 정책을 삭제하면 예외가 발생한다")
        void throwsException_whenPolicyNotFound() {
            // given
            given(policyRepository.findByName("INVALID")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> policyAdminService.deletePolicy("INVALID"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("역할에서 사용 중인 정책을 삭제하면 예외가 발생한다")
        void throwsException_whenPolicyInUse() {
            // given
            Policy policy = createPolicy(1L, "POLICY_USED");
            Role role = createRole(1L, "ROLE_USER");
            ReflectionTestUtils.setField(policy, "roles", Set.of(role));
            given(policyRepository.findByName("POLICY_USED")).willReturn(Optional.of(policy));

            // when & then
            assertThatThrownBy(() -> policyAdminService.deletePolicy("POLICY_USED"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("사용되지 않는 정책을 성공적으로 삭제한다")
        void deletesPolicySuccessfully() {
            // given
            Policy policy = createPolicy(1L, "POLICY_UNUSED");
            given(policyRepository.findByName("POLICY_UNUSED")).willReturn(Optional.of(policy));

            // when
            policyAdminService.deletePolicy("POLICY_UNUSED");

            // then
            verify(policyRepository).deleteByName("POLICY_UNUSED");
        }
    }

    @Nested
    @DisplayName("assignPermission 메서드")
    class AssignPermissionTest {

        @Test
        @DisplayName("존재하지 않는 정책에 권한을 할당하면 예외가 발생한다")
        void throwsException_whenPolicyNotFound() {
            // given
            given(policyRepository.findByName("INVALID")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> policyAdminService.assignPermission("INVALID", "PERM_READ"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("존재하지 않는 권한을 할당하면 예외가 발생한다")
        void throwsException_whenPermissionNotFound() {
            // given
            Policy policy = createPolicy(1L, "POLICY_READ");
            given(policyRepository.findByName("POLICY_READ")).willReturn(Optional.of(policy));
            given(permissionRepository.findByName("INVALID")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> policyAdminService.assignPermission("POLICY_READ", "INVALID"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("정책에 권한을 성공적으로 할당한다")
        void assignsPermissionSuccessfully() {
            // given
            Policy policy = createPolicy(1L, "POLICY_READ");
            Permission permission = createPermission(1L, "PERM_READ");
            given(policyRepository.findByName("POLICY_READ")).willReturn(Optional.of(policy));
            given(permissionRepository.findByName("PERM_READ")).willReturn(Optional.of(permission));

            // when
            policyAdminService.assignPermission("POLICY_READ", "PERM_READ");

            // then
            assertThat(policy.getPermissions()).contains(permission);
            verify(policyRepository).save(policy);
        }
    }

    @Nested
    @DisplayName("removePermission 메서드")
    class RemovePermissionTest {

        @Test
        @DisplayName("정책에서 권한을 성공적으로 제거한다")
        void removesPermissionSuccessfully() {
            // given
            Policy policy = createPolicy(1L, "POLICY_READ");
            Permission permission = createPermission(1L, "PERM_READ");
            policy.getPermissions().add(permission);
            
            given(policyRepository.findByName("POLICY_READ")).willReturn(Optional.of(policy));
            given(permissionRepository.findByName("PERM_READ")).willReturn(Optional.of(permission));

            // when
            policyAdminService.removePermission("POLICY_READ", "PERM_READ");

            // then
            assertThat(policy.getPermissions()).doesNotContain(permission);
            verify(policyRepository).save(policy);
        }
    }

    @Nested
    @DisplayName("getAllPermissions 메서드")
    class GetAllPermissionsTest {

        @Test
        @DisplayName("모든 권한 목록을 반환한다")
        void returnsAllPermissions() {
            // given
            Permission perm1 = createPermission(1L, "PERM_READ");
            Permission perm2 = createPermission(2L, "PERM_WRITE");
            given(permissionRepository.findAll()).willReturn(List.of(perm1, perm2));

            // when
            List<PermissionResponse> result = policyAdminService.getAllPermissions();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("권한이 없으면 빈 리스트를 반환한다")
        void returnsEmptyList_whenNoPermissions() {
            // given
            given(permissionRepository.findAll()).willReturn(List.of());

            // when
            List<PermissionResponse> result = policyAdminService.getAllPermissions();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPermission 메서드")
    class GetPermissionTest {

        @Test
        @DisplayName("존재하는 권한을 조회한다")
        void returnsPermission_whenExists() {
            // given
            Permission permission = createPermission(1L, "PERM_READ");
            given(permissionRepository.findByName("PERM_READ")).willReturn(Optional.of(permission));

            // when
            PermissionResponse result = policyAdminService.getPermission("PERM_READ");

            // then
            assertThat(result.name()).isEqualTo("PERM_READ");
        }

        @Test
        @DisplayName("존재하지 않는 권한을 조회하면 예외가 발생한다")
        void throwsException_whenPermissionNotFound() {
            // given
            given(permissionRepository.findByName("INVALID")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> policyAdminService.getPermission("INVALID"))
                .isInstanceOf(GlobalException.class);
        }
    }
}
