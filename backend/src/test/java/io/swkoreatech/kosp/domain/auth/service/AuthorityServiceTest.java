package io.swkoreatech.kosp.domain.auth.service;

import static io.swkoreatech.kosp.global.common.fixture.TestAuthFixture.*;
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

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorityService 단위 테스트")
class AuthorityServiceTest {

    @InjectMocks
    private AuthorityService authorityService;

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
            assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
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
            assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER", "PERM_READ_ARTICLE");
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
            assertThat(result)
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER", "ROLE_ADMIN", "PERM_READ", "PERM_ADMIN");
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
