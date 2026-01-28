package io.swkoreatech.kosp.domain.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.domain.admin.member.dto.request.AdminUserUpdateRequest;
import io.swkoreatech.kosp.domain.admin.member.dto.response.AdminUserListResponse;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.auth.repository.RoleRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminMemberService 단위 테스트")
class AdminMemberServiceTest {

    @InjectMocks
    private AdminMemberService adminMemberService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("encoded_password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
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
    @DisplayName("updateUserRoles 메서드")
    class UpdateUserRolesTest {

        @Test
        @DisplayName("존재하지 않는 사용자의 역할을 수정하면 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminMemberService.updateUserRoles(999L, Set.of("ROLE_USER")))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("존재하지 않는 역할로 수정하면 예외가 발생한다")
        void throwsException_whenRoleNotFound() {
            // given
            User user = createUser(1L, "홍길동");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(roleRepository.findByName("INVALID_ROLE")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminMemberService.updateUserRoles(1L, Set.of("INVALID_ROLE")))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("사용자의 역할을 성공적으로 수정한다")
        void updatesRolesSuccessfully() {
            // given
            User user = createUser(1L, "홍길동");
            Role roleUser = createRole(1L, "ROLE_USER");
            Role roleAdmin = createRole(2L, "ROLE_ADMIN");
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(roleUser));
            given(roleRepository.findByName("ROLE_ADMIN")).willReturn(Optional.of(roleAdmin));

            // when
            adminMemberService.updateUserRoles(1L, Set.of("ROLE_USER", "ROLE_ADMIN"));

            // then
            assertThat(user.getRoles()).hasSize(2);
            assertThat(user.getRoles()).containsExactlyInAnyOrder(roleUser, roleAdmin);
        }

        @Test
        @DisplayName("빈 역할 Set으로 수정하면 모든 역할이 제거된다")
        void removesAllRoles_whenEmptyRoleSet() {
            // given
            User user = createUser(1L, "홍길동");
            Role existingRole = createRole(1L, "ROLE_USER");
            user.getRoles().add(existingRole);
            
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            adminMemberService.updateUserRoles(1L, Set.of());

            // then
            assertThat(user.getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteUser 메서드")
    class DeleteUserTest {

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하면 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminMemberService.deleteUser(999L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("사용자를 성공적으로 소프트 삭제한다")
        void softDeletesUserSuccessfully() {
            // given
            User user = createUser(1L, "홍길동");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            adminMemberService.deleteUser(1L);

            // then
            assertThat(user.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("getUsers 메서드")
    class GetUsersTest {

        @Test
        @DisplayName("사용자 목록을 페이징하여 조회한다")
        void returnsPagedUserList() {
            // given
            User user1 = createUser(1L, "홍길동");
            User user2 = createUser(2L, "김철수");
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);
            
            given(userRepository.findAll(pageable)).willReturn(userPage);

            // when
            AdminUserListResponse result = adminMemberService.getUsers(pageable);

            // then
            assertThat(result.users()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.currentPage()).isEqualTo(0);
            assertThat(result.pageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("사용자가 없으면 빈 목록을 반환한다")
        void returnsEmptyList_whenNoUsers() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            
            given(userRepository.findAll(pageable)).willReturn(emptyPage);

            // when
            AdminUserListResponse result = adminMemberService.getUsers(pageable);

            // then
            assertThat(result.users()).isEmpty();
            assertThat(result.totalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("GithubUser가 있는 사용자의 프로필 이미지 URL을 포함한다")
        void includesProfileImageUrl_whenGithubUserExists() {
            // given
            User user = createUser(1L, "홍길동");
            GithubUser githubUser = GithubUser.builder()
                .githubId(12345L)
                .githubLogin("honggildong")
                .githubAvatarUrl("https://avatar.url")
                .build();
            ReflectionTestUtils.setField(user, "githubUser", githubUser);
            
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
            given(userRepository.findAll(pageable)).willReturn(userPage);

            // when
            AdminUserListResponse result = adminMemberService.getUsers(pageable);

            // then
            assertThat(result.users().get(0).profileImageUrl()).isEqualTo("https://avatar.url");
        }

        @Test
        @DisplayName("GithubUser가 없는 사용자의 프로필 이미지 URL은 null이다")
        void profileImageUrlIsNull_whenNoGithubUser() {
            // given
            User user = createUser(1L, "홍길동");
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
            given(userRepository.findAll(pageable)).willReturn(userPage);

            // when
            AdminUserListResponse result = adminMemberService.getUsers(pageable);

            // then
            assertThat(result.users().get(0).profileImageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("updateUser 메서드")
    class UpdateUserTest {

        @Test
        @DisplayName("존재하지 않는 사용자를 수정하면 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                "홍길동", "2024123456", "test@koreatech.ac.kr", null, null
            );

            // when & then
            assertThatThrownBy(() -> adminMemberService.updateUser(999L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("kutId가 다른 사용자와 중복되면 예외가 발생한다")
        void throwsException_whenKutIdAlreadyExists() {
            // given
            User user = createUser(1L, "사용자A");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByKutIdAndIdNot("2024000002", 1L)).willReturn(true);
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                "사용자A", "2024000002", "usera@koreatech.ac.kr", null, null
            );

            // when & then
            assertThatThrownBy(() -> adminMemberService.updateUser(1L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("kutEmail이 다른 사용자와 중복되면 예외가 발생한다")
        void throwsException_whenKutEmailAlreadyExists() {
            // given
            User user = createUser(1L, "사용자A");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByKutEmailAndIdNot("userb@koreatech.ac.kr", 1L)).willReturn(true);
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                "사용자A", "2024000001", "userb@koreatech.ac.kr", null, null
            );

            // when & then
            assertThatThrownBy(() -> adminMemberService.updateUser(1L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("자기 자신의 kutId와 kutEmail로 수정하면 성공한다")
        void successfullyUpdates_withSameKutIdAndKutEmail() {
            // given
            User user = createUser(1L, "사용자A");
            ReflectionTestUtils.setField(user, "kutId", "2024000001");
            ReflectionTestUtils.setField(user, "kutEmail", "usera@koreatech.ac.kr");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByKutIdAndIdNot("2024000001", 1L)).willReturn(false);
            given(userRepository.existsByKutEmailAndIdNot("usera@koreatech.ac.kr", 1L)).willReturn(false);
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                "사용자A", "2024000001", "usera@koreatech.ac.kr", "새 소개", null
            );

            // when
            adminMemberService.updateUser(1L, request);

            // then
            assertThat(user.getKutId()).isEqualTo("2024000001");
            assertThat(user.getKutEmail()).isEqualTo("usera@koreatech.ac.kr");
            assertThat(user.getIntroduction()).isEqualTo("새 소개");
        }

        @Test
        @DisplayName("kutEmail 대소문자 차이만 있어도 중복으로 감지한다")
        void throwsException_whenKutEmailDiffersByCaseOnly() {
            // given
            User user = createUser(1L, "사용자A");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByKutEmailAndIdNot("userb@koreatech.ac.kr", 1L)).willReturn(true);
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                "사용자A", "2024000001", "USERB@koreatech.ac.kr", null, null
            );

            // when & then
            assertThatThrownBy(() -> adminMemberService.updateUser(1L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("모든 필드를 성공적으로 수정한다")
        void successfullyUpdatesAllFields() {
            // given
            User user = createUser(1L, "사용자A");
            GithubUser githubUser = GithubUser.builder()
                .githubId(12345L)
                .githubLogin("usera")
                .githubAvatarUrl("https://old.url")
                .build();
            ReflectionTestUtils.setField(user, "githubUser", githubUser);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByKutIdAndIdNot("2024999999", 1L)).willReturn(false);
            given(userRepository.existsByKutEmailAndIdNot("newemail@koreatech.ac.kr", 1L)).willReturn(false);
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                "새이름", "2024999999", "newemail@koreatech.ac.kr", "새 소개", "https://new.url"
            );

            // when
            adminMemberService.updateUser(1L, request);

            // then
            assertThat(user.getName()).isEqualTo("새이름");
            assertThat(user.getKutId()).isEqualTo("2024999999");
            assertThat(user.getKutEmail()).isEqualTo("newemail@koreatech.ac.kr");
            assertThat(user.getIntroduction()).isEqualTo("새 소개");
            assertThat(user.getGithubUser().getGithubAvatarUrl()).isEqualTo("https://new.url");
        }

        @Test
        @DisplayName("Null 필드는 업데이트하지 않는다")
        void doesNotUpdate_whenFieldsAreNull() {
            // given
            User user = createUser(1L, "사용자A");
            ReflectionTestUtils.setField(user, "kutId", "2024000001");
            ReflectionTestUtils.setField(user, "kutEmail", "usera@koreatech.ac.kr");
            ReflectionTestUtils.setField(user, "introduction", "기존 소개");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            AdminUserUpdateRequest request = new AdminUserUpdateRequest(
                "사용자A", "2024000001", "usera@koreatech.ac.kr", null, null
            );

            // when
            adminMemberService.updateUser(1L, request);

            // then
            assertThat(user.getIntroduction()).isEqualTo("기존 소개");
        }
    }
}
