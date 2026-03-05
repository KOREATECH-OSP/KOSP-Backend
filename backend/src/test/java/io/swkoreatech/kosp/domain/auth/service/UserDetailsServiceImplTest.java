package io.swkoreatech.kosp.domain.auth.service;

import static io.swkoreatech.kosp.global.common.fixture.TestAuthFixture.*;
import static io.swkoreatech.kosp.global.common.fixture.TestUserFixture.createUserWithEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl 단위 테스트")
class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

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
            User deletedUser = createUserWithEmail(1L, "deleted@koreatech.ac.kr", true);
            given(userRepository.findByKutEmail("deleted@koreatech.ac.kr")).willReturn(Optional.of(deletedUser));

            // when & then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("deleted@koreatech.ac.kr"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("정상 사용자 조회 시 UserDetails를 반환한다")
        void returnsUserDetails_whenUserExists() {
            // given
            User user = createUserWithEmail(1L, "user@koreatech.ac.kr", false);
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
            User user = createUserWithEmail(1L, "user@koreatech.ac.kr", false);
            Role role = createRole(1L, "ROLE_USER");
            user.getRoles().add(role);
            given(userRepository.findByKutEmail("user@koreatech.ac.kr")).willReturn(Optional.of(user));

            // when
            UserDetails result = userDetailsService.loadUserByUsername("user@koreatech.ac.kr");

            // then
            assertThat(result.getAuthorities()).isNotEmpty();
            assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
        }

        @Test
        @DisplayName("SUPERUSER 역할을 가진 사용자는 와일드카드 권한을 갖는다")
        void grantsSuperuserWildcard_whenUserIsSuperuser() {
            // given
            User user = createUserWithEmail(1L, "admin@koreatech.ac.kr", false);
            Role superuserRole = createRole(1L, "ROLE_SUPERUSER");
            user.getRoles().add(superuserRole);
            given(userRepository.findByKutEmail("admin@koreatech.ac.kr")).willReturn(Optional.of(user));

            // when
            UserDetails result = userDetailsService.loadUserByUsername("admin@koreatech.ac.kr");

            // then
            assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("*");
        }

        @Test
        @DisplayName("역할에 연결된 정책의 권한을 추출한다")
        void extractsPermissions_fromPolicies() {
            // given
            User user = createUserWithEmail(1L, "user@koreatech.ac.kr", false);
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
            assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("PERM_READ_ARTICLE");
        }
    }
}
