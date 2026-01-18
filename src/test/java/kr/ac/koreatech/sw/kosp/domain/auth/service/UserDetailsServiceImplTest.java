package kr.ac.koreatech.sw.kosp.domain.auth.service;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl 단위 테스트")
class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    private User createUser(Long id, String email, boolean isDeleted) {
        User user = User.builder()
            .name("테스터")
            .kutId("2024" + id)
            .kutEmail(email)
            .password("encoded_password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "isDeleted", isDeleted);
        return user;
    }

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
            .permissions(new HashSet<>())
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

    @Nested
    @DisplayName("loadUserByUsername 메서드")
    class LoadUserByUsernameTest {

        @Test
        @DisplayName("존재하지 않는 이메일로 조회하면 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            given(userRepository.findByKutEmail("notfound@koreatech.ac.kr")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("notfound@koreatech.ac.kr"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("탈퇴한 사용자로 조회하면 예외가 발생한다")
        void throwsException_whenUserIsDeleted() {
            // given
            User deletedUser = createUser(1L, "deleted@koreatech.ac.kr", true);
            given(userRepository.findByKutEmail("deleted@koreatech.ac.kr")).willReturn(Optional.of(deletedUser));

            // when & then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("deleted@koreatech.ac.kr"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("정상 사용자 조회 시 UserDetails를 반환한다")
        void returnsUserDetails_whenUserExists() {
            // given
            User user = createUser(1L, "user@koreatech.ac.kr", false);
            given(userRepository.findByKutEmail("user@koreatech.ac.kr")).willReturn(Optional.of(user));

            // when
            UserDetails result = userDetailsService.loadUserByUsername("user@koreatech.ac.kr");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("user@koreatech.ac.kr");
        }

        @Test
        @DisplayName("역할이 있는 사용자의 권한을 설정한다")
        void setsAuthorities_whenUserHasRoles() {
            // given
            User user = createUser(1L, "user@koreatech.ac.kr", false);
            Role role = createRole(1L, "ROLE_USER");
            user.getRoles().add(role);
            given(userRepository.findByKutEmail("user@koreatech.ac.kr")).willReturn(Optional.of(user));

            // when
            UserDetails result = userDetailsService.loadUserByUsername("user@koreatech.ac.kr");

            // then
            assertThat(result.getAuthorities()).isNotEmpty();
            assertThat(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue();
        }

        @Test
        @DisplayName("SUPERUSER 역할을 가진 사용자는 와일드카드 권한을 갖는다")
        void grantsSuperuserWildcard_whenUserIsSuperuser() {
            // given
            User user = createUser(1L, "admin@koreatech.ac.kr", false);
            Role superuserRole = createRole(1L, "ROLE_SUPERUSER");
            user.getRoles().add(superuserRole);
            given(userRepository.findByKutEmail("admin@koreatech.ac.kr")).willReturn(Optional.of(user));

            // when
            UserDetails result = userDetailsService.loadUserByUsername("admin@koreatech.ac.kr");

            // then
            assertThat(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("*"))).isTrue();
        }

        @Test
        @DisplayName("역할에 연결된 정책의 권한을 추출한다")
        void extractsPermissions_fromPolicies() {
            // given
            User user = createUser(1L, "user@koreatech.ac.kr", false);
            Role role = createRole(1L, "ROLE_USER");
            Policy policy = createPolicy(1L, "POLICY_READ");
            Permission permission = createPermission(1L, "PERM_READ_ARTICLE");
            
            policy.getPermissions().add(permission);
            role.getPolicies().add(policy);
            user.getRoles().add(role);
            
            given(userRepository.findByKutEmail("user@koreatech.ac.kr")).willReturn(Optional.of(user));

            // when
            UserDetails result = userDetailsService.loadUserByUsername("user@koreatech.ac.kr");

            // then
            assertThat(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("PERM_READ_ARTICLE"))).isTrue();
        }
    }
}
