package kr.ac.koreatech.sw.kosp.domain.github.service;

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

import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubAnalysisResponse;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubProfileRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("GithubService 단위 테스트")
class GithubServiceTest {

    @InjectMocks
    private GithubService githubService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GithubProfileRepository githubProfileRepository;

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

    private GithubUser createGithubUser(Long githubId, String login) {
        GithubUser githubUser = GithubUser.builder()
            .githubId(githubId)
            .githubLogin(login)
            .build();
        return githubUser;
    }

    private GithubProfile createGithubProfile(Long githubId) {
        GithubProfile profile = GithubProfile.builder()
            .githubId(githubId)
            .build();
        return profile;
    }

    @Nested
    @DisplayName("getAnalysis 메서드")
    class GetAnalysisTest {

        @Test
        @DisplayName("GitHub 사용자가 연결되어 있지 않으면 예외가 발생한다")
        void throwsException_whenGithubUserNotLinked() {
            // given
            User user = createUser(1L, "testuser");
            given(userRepository.getById(1L)).willReturn(user);

            // when & then
            assertThatThrownBy(() -> githubService.getAnalysis(1L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("GitHub 프로필이 존재하지 않으면 예외가 발생한다")
        void throwsException_whenProfileNotFound() {
            // given
            User user = createUser(1L, "testuser");
            GithubUser githubUser = createGithubUser(123L, "testlogin");
            ReflectionTestUtils.setField(user, "githubUser", githubUser);
            
            given(userRepository.getById(1L)).willReturn(user);
            given(githubProfileRepository.findByGithubId(123L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> githubService.getAnalysis(1L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("GitHub 분석 결과를 정상적으로 반환한다")
        void returnsAnalysisResponse() {
            // given
            User user = createUser(1L, "testuser");
            GithubUser githubUser = createGithubUser(123L, "testlogin");
            ReflectionTestUtils.setField(user, "githubUser", githubUser);
            GithubProfile profile = createGithubProfile(123L);
            
            given(userRepository.getById(1L)).willReturn(user);
            given(githubProfileRepository.findByGithubId(123L)).willReturn(Optional.of(profile));

            // when
            GithubAnalysisResponse response = githubService.getAnalysis(1L);

            // then
            assertThat(response).isNotNull();
        }
    }
}
