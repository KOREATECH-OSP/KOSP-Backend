package io.swkoreatech.kosp.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.auth.model.Permission;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService 단위 테스트")
class PermissionServiceTest {

    @InjectMocks
    private PermissionService permissionService;

    @Mock
    private UserRepository userRepository;

    private User createUser(Long id) {
        User user = User.builder()
            .name("테스터")
            .kutId("2024" + id)
            .kutEmail("user" + id + "@koreatech.ac.kr")
            .password("encoded_password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Role createRole(Long id, String name) {
        Role role = Role.builder()
            .name(name)
            .policies(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    private Policy createPolicy(Long id, String name) {
        Policy policy = Policy.builder()
            .name(name)
            .permissions(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(policy, "id", id);
        return policy;
    }

    private Permission createPermission(Long id, String name) {
        Permission permission = Permission.builder()
            .name(name)
            .build();
        ReflectionTestUtils.setField(permission, "id", id);
        return permission;
    }

    @Nested
    @DisplayName("hasPermission 메서드")
    class HasPermissionTest {

        @Test
        @DisplayName("존재하지 않는 사용자의 권한을 확인하면 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            given(userRepository.findByIdWithRolesAndPermissions(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> permissionService.hasPermission(999L, "PERM_READ"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("권한이 없는 사용자는 false를 반환한다")
        void returnsFalse_whenUserHasNoPermission() {
            // given
            User user = createUser(1L);
            given(userRepository.findByIdWithRolesAndPermissions(1L)).willReturn(Optional.of(user));

            // when
            boolean result = permissionService.hasPermission(1L, "PERM_READ");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("권한이 있는 사용자는 true를 반환한다")
        void returnsTrue_whenUserHasPermission() {
            // given
            User user = createUser(1L);
            Role role = createRole(1L, "ROLE_USER");
            Policy policy = createPolicy(1L, "POLICY_READ");
            Permission permission = createPermission(1L, "PERM_READ");
            
            policy.getPermissions().add(permission);
            role.getPolicies().add(policy);
            user.getRoles().add(role);
            
            given(userRepository.findByIdWithRolesAndPermissions(1L)).willReturn(Optional.of(user));

            // when
            boolean result = permissionService.hasPermission(1L, "PERM_READ");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 권한을 가진 사용자는 false를 반환한다")
        void returnsFalse_whenUserHasDifferentPermission() {
            // given
            User user = createUser(1L);
            Role role = createRole(1L, "ROLE_USER");
            Policy policy = createPolicy(1L, "POLICY_WRITE");
            Permission permission = createPermission(1L, "PERM_WRITE");
            
            policy.getPermissions().add(permission);
            role.getPolicies().add(policy);
            user.getRoles().add(role);
            
            given(userRepository.findByIdWithRolesAndPermissions(1L)).willReturn(Optional.of(user));

            // when
            boolean result = permissionService.hasPermission(1L, "PERM_READ");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("여러 역할 중 하나라도 권한이 있으면 true를 반환한다")
        void returnsTrue_whenAnyRoleHasPermission() {
            // given
            User user = createUser(1L);
            
            Role role1 = createRole(1L, "ROLE_USER");
            Policy policy1 = createPolicy(1L, "POLICY_BASIC");
            
            Role role2 = createRole(2L, "ROLE_EDITOR");
            Policy policy2 = createPolicy(2L, "POLICY_EDIT");
            Permission permission = createPermission(1L, "PERM_EDIT");
            
            policy2.getPermissions().add(permission);
            role1.getPolicies().add(policy1);
            role2.getPolicies().add(policy2);
            user.getRoles().add(role1);
            user.getRoles().add(role2);
            
            given(userRepository.findByIdWithRolesAndPermissions(1L)).willReturn(Optional.of(user));

            // when
            boolean result = permissionService.hasPermission(1L, "PERM_EDIT");

            // then
            assertThat(result).isTrue();
        }
    }
}
