package io.swkoreatech.kosp.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.auth.dto.response.CheckMemberIdResponse;
import io.swkoreatech.kosp.domain.auth.repository.RoleRepository;
import io.swkoreatech.kosp.domain.auth.service.AuthService;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubUserRepository;
import io.swkoreatech.kosp.domain.user.dto.request.UserSignupRequest;
import io.swkoreatech.kosp.domain.user.dto.request.UserUpdateRequest;
import io.swkoreatech.kosp.domain.user.dto.response.MyApplicationListResponse;
import io.swkoreatech.kosp.domain.user.dto.response.UserProfileResponse;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.auth.token.SignupToken;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GithubUserRepository githubUserRepository;

    @Mock
    private RecruitApplyRepository recruitApplyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private SignupToken createSignupToken(boolean emailVerified) {
        return SignupToken.builder()
            .githubId("123")
            .login("testuser")
            .name("Test User")
            .avatarUrl("https://avatar.url")
            .encryptedGithubToken("encrypted-token")
            .kutEmail("test@koreatech.ac.kr")
            .emailVerified(emailVerified)
            .build();
    }

    @Nested
    @DisplayName("signup 메서드")
    class SignupTest {

        @Test
        @DisplayName("이메일이 인증되지 않으면 예외가 발생한다")
        void throwsException_whenEmailNotVerified() {
            // given
            UserSignupRequest request = new UserSignupRequest("홍길동", "2024123456", "test@koreatech.ac.kr", "Password1!");
            SignupToken token = createSignupToken(false);

            // when & then
            assertThatThrownBy(() -> userService.signup(request, token))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("이미 존재하는 활성 사용자가 있으면 예외가 발생한다")
        void throwsException_whenUserAlreadyExists() {
            // given
            UserSignupRequest request = new UserSignupRequest("홍길동", "2024123456", "test@koreatech.ac.kr", "Password1!");
            SignupToken token = createSignupToken(true);
            User existingUser = createUser(1L, "기존유저");
            
            given(githubUserRepository.findByGithubId(123L)).willReturn(Optional.empty());
            given(userRepository.findByKutEmail("test@koreatech.ac.kr")).willReturn(Optional.of(existingUser));

            // when & then
            assertThatThrownBy(() -> userService.signup(request, token))
                .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("update 메서드")
    class UpdateTest {

        @Test
        @DisplayName("사용자 정보를 성공적으로 수정한다")
        void updatesUserInfo() {
            // given
            User user = createUser(1L, "홍길동");
            UserUpdateRequest request = new UserUpdateRequest("김철수", "안녕하세요");
            given(userRepository.getById(1L)).willReturn(user);

            // when
            userService.update(1L, request);

            // then
            assertThat(user.getName()).isEqualTo("김철수");
            assertThat(user.getIntroduction()).isEqualTo("안녕하세요");
        }
    }

    @Nested
    @DisplayName("getProfile 메서드")
    class GetProfileTest {

        @Test
        @DisplayName("사용자 프로필을 조회한다")
        void returnsUserProfile() {
            // given
            User user = createUser(1L, "홍길동");
            given(userRepository.getById(1L)).willReturn(user);

            // when
            UserProfileResponse response = userService.getProfile(1L);

            // then
            assertThat(response.name()).isEqualTo("홍길동");
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteTest {

        @Test
        @DisplayName("사용자를 소프트 삭제한다")
        void softDeletesUser() {
            // given
            User user = createUser(1L, "홍길동");
            given(userRepository.getById(1L)).willReturn(user);

            // when
            userService.delete(1L);

            // then
            assertThat(user.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("changePassword 메서드")
    class ChangePasswordTest {

        @Test
        @DisplayName("현재 비밀번호가 일치하지 않으면 예외가 발생한다")
        void throwsException_whenCurrentPasswordDoesNotMatch() {
            // given
            User user = createUser(1L, "홍길동");
            given(userRepository.getById(1L)).willReturn(user);
            given(passwordEncoder.matches("wrongPassword", "password")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.changePassword(1L, "wrongPassword", "newPassword"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("비밀번호를 성공적으로 변경한다")
        void changesPassword_whenCurrentPasswordMatches() {
            // given
            User user = createUser(1L, "홍길동");
            given(userRepository.getById(1L)).willReturn(user);
            given(passwordEncoder.matches("currentPassword", "password")).willReturn(true);

            // when
            userService.changePassword(1L, "currentPassword", "newPassword");

            // then
            verify(passwordEncoder).encode("newPassword");
        }
    }

    @Nested
    @DisplayName("checkMemberIdAvailability 메서드")
    class CheckMemberIdAvailabilityTest {

        @Test
        @DisplayName("학번(10자리)이 사용 가능하면 해당 메시지를 반환한다")
        void returnsAvailable_forNewStudentId() {
            // given
            String memberId = "2024123456";
            given(userRepository.existsByKutIdAndIsDeletedFalse(memberId)).willReturn(false);

            // when
            CheckMemberIdResponse response = userService.checkMemberIdAvailability(memberId);

            // then
            assertThat(response.available()).isTrue();
            assertThat(response.message()).contains("사용 가능한 학번");
        }

        @Test
        @DisplayName("학번(10자리)이 이미 존재하면 해당 메시지를 반환한다")
        void returnsNotAvailable_forExistingStudentId() {
            // given
            String memberId = "2024123456";
            given(userRepository.existsByKutIdAndIsDeletedFalse(memberId)).willReturn(true);

            // when
            CheckMemberIdResponse response = userService.checkMemberIdAvailability(memberId);

            // then
            assertThat(response.available()).isFalse();
            assertThat(response.message()).contains("이미 가입된 학번");
        }

        @Test
        @DisplayName("사번(10자리 미만)이면 사번으로 분류한다")
        void classifiesAsEmployeeId_whenLessThan10Digits() {
            // given
            String memberId = "123456";
            given(userRepository.existsByKutIdAndIsDeletedFalse(memberId)).willReturn(false);

            // when
            CheckMemberIdResponse response = userService.checkMemberIdAvailability(memberId);

            // then
            assertThat(response.message()).contains("사번");
        }
    }

    @Nested
    @DisplayName("getMyApplications 메서드")
    class GetMyApplicationsTest {

        @Test
        @DisplayName("사용자의 지원 목록을 페이징하여 조회한다")
        void returnsPagedApplications() {
            // given
            User user = createUser(1L, "홍길동");
            Pageable pageable = PageRequest.of(0, 10);
            given(recruitApplyRepository.findByUser(user, pageable))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

            // when
            MyApplicationListResponse response = userService.getMyApplications(user, pageable);

            // then
            assertThat(response.applications()).isEmpty();
        }
    }
}
