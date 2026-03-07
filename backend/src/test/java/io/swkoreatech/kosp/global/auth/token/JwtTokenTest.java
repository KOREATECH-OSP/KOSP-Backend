package io.swkoreatech.kosp.global.auth.token;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("JwtToken 통합 테스트")
class JwtTokenTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("AccessToken")
    class AccessTokenTest {

        @Test
        @DisplayName("loginToken을 생성하고 파싱한다")
        void createsAndParsesLoginToken() {
            // given
            User user = User.builder()
                .name("Test User")
                .kutId("2020136000")
                .kutEmail("test@koreatech.ac.kr")
                .password("password")
                .build();
            user = userRepository.save(user);

            // when
            AccessToken originalToken = AccessToken.from(user);
            String jwtString = originalToken.toString();

            // then
            assertThat(jwtString).isNotNull().contains(".");

            AccessToken parsedToken = JwtToken.from(AccessToken.class, jwtString);
            assertThat(parsedToken.getUserId()).isEqualTo(user.getId());
            assertThat(parsedToken.getKutEmail()).isEqualTo("test@koreatech.ac.kr");
            assertThat(parsedToken.getName()).isEqualTo("Test User");
        }
    }

    @Nested
    @DisplayName("RefreshToken")
    class RefreshTokenTest {

        @Test
        @DisplayName("refreshToken을 생성하고 파싱한다")
        void createsAndParsesRefreshToken() {
            // given
            User user = User.builder()
                .name("Test User")
                .kutId("2020136000")
                .kutEmail("test@koreatech.ac.kr")
                .password("password")
                .build();
            user = userRepository.save(user);

            // when
            RefreshToken originalToken = RefreshToken.from(user);
            String jwtString = originalToken.toString();

            // then
            assertThat(jwtString).isNotNull();

            RefreshToken parsedToken = JwtToken.from(RefreshToken.class, jwtString);
            assertThat(parsedToken.getUserId()).isEqualTo(user.getId());
        }
    }

    @Nested
    @DisplayName("SignupToken")
    class SignupTokenTest {

        @Test
        @DisplayName("signupToken을 생성하고 파싱한다")
        void createsAndParsesSignupToken() {
            // given
            SignupToken originalToken = SignupToken.fromGithub(
                "12345", "testuser", "Test User",
                "https://avatar.url", "encryptedToken"
            );

            // when
            String jwtString = originalToken.toString();

            // then
            assertThat(jwtString).isNotNull();

            SignupToken parsedToken = JwtToken.from(SignupToken.class, jwtString);
            assertThat(parsedToken.getGithubId()).isEqualTo("12345");
            assertThat(parsedToken.isEmailVerified()).isFalse();
        }
    }
}
