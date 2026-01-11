package kr.ac.koreatech.sw.kosp.global.auth.token;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JwtTokenTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void loginToken_생성_및_파싱_테스트() {
        // given: 실제 User 저장
        User user = User.builder()
            .name("Test User")
            .kutId("2020136000")
            .kutEmail("test@koreatech.ac.kr")
            .password("password")
            .build();
        user = userRepository.save(user);
        
        // when: 토큰 생성
        AccessToken originalToken = AccessToken.from(user);
        String jwtString = originalToken.toString();
        
        System.out.println("Generated JWT: " + jwtString);
        assertThat(jwtString).isNotNull().contains(".");
        
        // then: 토큰 파싱
        AccessToken parsedToken = JwtToken.from(AccessToken.class, jwtString);
        
        assertThat(parsedToken.getUserId()).isEqualTo(user.getId());
        assertThat(parsedToken.getKutEmail()).isEqualTo("test@koreatech.ac.kr");
        assertThat(parsedToken.getName()).isEqualTo("Test User");
    }
    
    @Test
    void refreshToken_생성_및_파싱_테스트() {
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
        
        System.out.println("Generated Refresh JWT: " + jwtString);
        assertThat(jwtString).isNotNull();
        
        // then
        RefreshToken parsedToken = JwtToken.from(RefreshToken.class, jwtString);
        
        assertThat(parsedToken.getUserId()).isEqualTo(user.getId());
    }
    @Test
    void SignupToken_생성_및_파싱_테스트() {
        // given
        SignupToken originalToken = SignupToken.fromGithub(
            "12345", "testuser", "Test User", "https://avatar.url", "encryptedToken"
        );
        String jwtString = originalToken.toString();
        
        System.out.println("Generated Signup JWT: " + jwtString);
        assertThat(jwtString).isNotNull();
        
        // then
        SignupToken parsedToken = JwtToken.from(SignupToken.class, jwtString);
        
        assertThat(parsedToken.getGithubId()).isEqualTo("12345");
        assertThat(parsedToken.isEmailVerified()).isFalse();
    }
}
