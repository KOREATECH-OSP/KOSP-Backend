package io.swkoreatech.kosp.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.auth.model.Permission;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.domain.auth.model.Role;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorityService 단위 테스트")
class AuthorityServiceTest {

    @InjectMocks
    private AuthorityService authorityService;

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
    @DisplayName("getAuthorities 메서드")
    class GetAuthoritiesTest {

        @Test
        @DisplayName("빈 역할 Set이면 빈 권한을 반환한다")
        void returnsEmptyAuthorities_whenNoRoles() {
            // given
            Set<Role> roles = new HashSet<>();

            // when
            Collection<? extends GrantedAuthority> result = authorityService.getAuthorities(roles);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("역할 이름을 권한으로 포함한다")
        void includesRoleName_asAuthority() {
            // given
            Set<Role> roles = new HashSet<>();
            Role role = createRole(1L, "ROLE_USER");
            roles.add(role);

            // when
            Collection<? extends GrantedAuthority> result = authorityService.getAuthorities(roles);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue();
        }

        @Test
        @DisplayName("정책에 연결된 권한을 포함한다")
        void includesPermissions_fromPolicies() {
            // given
            Set<Role> roles = new HashSet<>();
            Role role = createRole(1L, "ROLE_USER");
            Policy policy = createPolicy(1L, "POLICY_READ");
            Permission permission = createPermission(1L, "PERM_READ_ARTICLE");
            
            policy.getPermissions().add(permission);
            role.getPolicies().add(policy);
            roles.add(role);

            // when
            Collection<? extends GrantedAuthority> result = authorityService.getAuthorities(roles);

            // then
            assertThat(result.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue();
            assertThat(result.stream()
                .anyMatch(a -> a.getAuthority().equals("PERM_READ_ARTICLE"))).isTrue();
        }

        @Test
        @DisplayName("여러 역할의 권한을 모두 포함한다")
        void includesAllAuthorities_fromMultipleRoles() {
            // given
            Set<Role> roles = new HashSet<>();
            
            Role role1 = createRole(1L, "ROLE_USER");
            Policy policy1 = createPolicy(1L, "POLICY_READ");
            Permission perm1 = createPermission(1L, "PERM_READ");
            policy1.getPermissions().add(perm1);
            role1.getPolicies().add(policy1);
            
            Role role2 = createRole(2L, "ROLE_ADMIN");
            Policy policy2 = createPolicy(2L, "POLICY_ADMIN");
            Permission perm2 = createPermission(2L, "PERM_ADMIN");
            policy2.getPermissions().add(perm2);
            role2.getPolicies().add(policy2);
            
            roles.add(role1);
            roles.add(role2);

            // when
            Collection<? extends GrantedAuthority> result = authorityService.getAuthorities(roles);

            // then
            assertThat(result.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue();
            assertThat(result.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))).isTrue();
            assertThat(result.stream()
                .anyMatch(a -> a.getAuthority().equals("PERM_READ"))).isTrue();
            assertThat(result.stream()
                .anyMatch(a -> a.getAuthority().equals("PERM_ADMIN"))).isTrue();
        }

        @Test
        @DisplayName("중복된 권한은 하나만 포함한다")
        void deduplicates_samePermissions() {
            // given
            Set<Role> roles = new HashSet<>();
            
            Role role1 = createRole(1L, "ROLE_USER");
            Policy policy1 = createPolicy(1L, "POLICY_READ");
            Permission sharedPerm = createPermission(1L, "PERM_SHARED");
            policy1.getPermissions().add(sharedPerm);
            role1.getPolicies().add(policy1);
            
            Role role2 = createRole(2L, "ROLE_ADMIN");
            Policy policy2 = createPolicy(2L, "POLICY_ADMIN");
            policy2.getPermissions().add(sharedPerm); // same permission
            role2.getPolicies().add(policy2);
            
            roles.add(role1);
            roles.add(role2);

            // when
            Collection<? extends GrantedAuthority> result = authorityService.getAuthorities(roles);

            // then
            long sharedPermCount = result.stream()
                .filter(a -> a.getAuthority().equals("PERM_SHARED"))
                .count();
            assertThat(sharedPermCount).isEqualTo(1);
        }
    }
}
