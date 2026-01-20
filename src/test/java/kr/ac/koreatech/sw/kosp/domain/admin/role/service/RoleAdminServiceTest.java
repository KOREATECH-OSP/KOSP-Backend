package kr.ac.koreatech.sw.kosp.domain.admin.role.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.RoleResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PolicyRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleAdminService 단위 테스트")
class RoleAdminServiceTest {

    @InjectMocks
    private RoleAdminService roleAdminService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private UserRepository userRepository;

    private Role createRole(Long id, String name) {
        Role role = Role.builder()
            .name(name)
            .description(name + " 역할")
            .policies(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    private Policy createPolicy(Long id, String name) {
        Policy policy = Policy.builder()
            .name(name)
            .description(name + " 정책")
            .build();
        ReflectionTestUtils.setField(policy, "id", id);
        return policy;
    }

    @Nested
    @DisplayName("getAllRoles 메서드")
    class GetAllRolesTest {

        @Test
        @DisplayName("모든 역할 목록을 반환한다")
        void returnsAllRoles() {
            // given
            Role role1 = createRole(1L, "ROLE_USER");
            Role role2 = createRole(2L, "ROLE_ADMIN");
            given(roleRepository.findAll()).willReturn(List.of(role1, role2));

            // when
            List<RoleResponse> result = roleAdminService.getAllRoles();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("역할이 없으면 빈 리스트를 반환한다")
        void returnsEmptyList_whenNoRoles() {
            // given
            given(roleRepository.findAll()).willReturn(List.of());

            // when
            List<RoleResponse> result = roleAdminService.getAllRoles();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRole 메서드")
    class GetRoleTest {

        @Test
        @DisplayName("존재하는 역할을 조회한다")
        void returnsRole_whenExists() {
            // given
            Role role = createRole(1L, "ROLE_USER");
            given(roleRepository.getByName("ROLE_USER")).willReturn(role);

            // when
            RoleResponse result = roleAdminService.getRole("ROLE_USER");

            // then
            assertThat(result.name()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("존재하지 않는 역할을 조회하면 예외가 발생한다")
        void throwsException_whenRoleNotFound() {
            // given
            given(roleRepository.getByName("INVALID")).willThrow(new GlobalException(ExceptionMessage.NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> roleAdminService.getRole("INVALID"))
                .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("createRole 메서드")
    class CreateRoleTest {

        @Test
        @DisplayName("중복된 이름의 역할을 생성하면 예외가 발생한다")
        void throwsException_whenDuplicateName() {
            // given
            given(roleRepository.existsByName("ROLE_USER")).willReturn(true);
            RoleRequest request = new RoleRequest("ROLE_USER", "사용자 역할", false);

            // when & then
            assertThatThrownBy(() -> roleAdminService.createRole(request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("새 역할을 성공적으로 생성한다")
        void createsRoleSuccessfully() {
            // given
            given(roleRepository.existsByName("ROLE_NEW")).willReturn(false);
            RoleRequest request = new RoleRequest("ROLE_NEW", "새 역할", false);

            // when
            roleAdminService.createRole(request);

            // then
            verify(roleRepository).save(any(Role.class));
        }
    }

    @Nested
    @DisplayName("updateRole 메서드")
    class UpdateRoleTest {

        @Test
        @DisplayName("ROLE_SUPERUSER를 수정하면 예외가 발생한다")
        void throwsException_whenUpdatingSuperuser() {
            // given
            RoleUpdateRequest request = new RoleUpdateRequest("수정된 설명", false);

            // when & then
            assertThatThrownBy(() -> roleAdminService.updateRole("ROLE_SUPERUSER", request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("역할 설명을 성공적으로 수정한다")
        void updatesDescriptionSuccessfully() {
            // given
            Role role = createRole(1L, "ROLE_USER");
            given(roleRepository.getByName("ROLE_USER")).willReturn(role);
            RoleUpdateRequest request = new RoleUpdateRequest("수정된 설명", false);

            // when
            roleAdminService.updateRole("ROLE_USER", request);

            // then
            assertThat(role.getDescription()).isEqualTo("수정된 설명");
        }
    }

    @Nested
    @DisplayName("deleteRole 메서드")
    class DeleteRoleTest {

        @Test
        @DisplayName("ROLE_SUPERUSER를 삭제하면 예외가 발생한다")
        void throwsException_whenDeletingSuperuser() {
            // when & then
            assertThatThrownBy(() -> roleAdminService.deleteRole("ROLE_SUPERUSER"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("사용 중인 역할을 삭제하면 예외가 발생한다")
        void throwsException_whenRoleInUse() {
            // given
            Role role = createRole(1L, "ROLE_USER");
            given(roleRepository.getByName("ROLE_USER")).willReturn(role);
            given(userRepository.existsByRoles_Name("ROLE_USER")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> roleAdminService.deleteRole("ROLE_USER"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("사용되지 않는 역할을 성공적으로 삭제한다")
        void deletesRoleSuccessfully() {
            // given
            Role role = createRole(1L, "ROLE_UNUSED");
            given(roleRepository.getByName("ROLE_UNUSED")).willReturn(role);
            given(userRepository.existsByRoles_Name("ROLE_UNUSED")).willReturn(false);

            // when
            roleAdminService.deleteRole("ROLE_UNUSED");

            // then
            verify(roleRepository).deleteByName("ROLE_UNUSED");
        }
    }

    @Nested
    @DisplayName("assignPolicy 메서드")
    class AssignPolicyTest {

        @Test
        @DisplayName("ROLE_SUPERUSER에 정책을 할당하면 예외가 발생한다")
        void throwsException_whenAssigningToSuperuser() {
            // when & then
            assertThatThrownBy(() -> roleAdminService.assignPolicy("ROLE_SUPERUSER", "POLICY_READ"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("역할에 정책을 성공적으로 할당한다")
        void assignsPolicySuccessfully() {
            // given
            Role role = createRole(1L, "ROLE_USER");
            Policy policy = createPolicy(1L, "POLICY_READ");
            given(roleRepository.getByName("ROLE_USER")).willReturn(role);
            given(policyRepository.getByName("POLICY_READ")).willReturn(policy);

            // when
            roleAdminService.assignPolicy("ROLE_USER", "POLICY_READ");

            // then
            assertThat(role.getPolicies()).contains(policy);
        }
    }

    @Nested
    @DisplayName("removePolicy 메서드")
    class RemovePolicyTest {

        @Test
        @DisplayName("ROLE_SUPERUSER에서 정책을 제거하면 예외가 발생한다")
        void throwsException_whenRemovingFromSuperuser() {
            // when & then
            assertThatThrownBy(() -> roleAdminService.removePolicy("ROLE_SUPERUSER", "POLICY_READ"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("역할에서 정책을 성공적으로 제거한다")
        void removesPolicySuccessfully() {
            // given
            Role role = createRole(1L, "ROLE_USER");
            Policy policy = createPolicy(1L, "POLICY_READ");
            role.getPolicies().add(policy);
            
            given(roleRepository.getByName("ROLE_USER")).willReturn(role);
            given(policyRepository.getByName("POLICY_READ")).willReturn(policy);

            // when
            roleAdminService.removePolicy("ROLE_USER", "POLICY_READ");

            // then
            assertThat(role.getPolicies()).doesNotContain(policy);
        }
    }
}
