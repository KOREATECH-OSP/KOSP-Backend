package kr.ac.koreatech.sw.kosp.global.auth.token;

import kr.ac.koreatech.sw.kosp.global.auth.annotation.TokenSpec;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 토큰
 */
@Getter
@Builder
@TokenSpec(TokenType.SIGNUP)
public class SignupToken extends JwtToken {

    // 필수 필드 (GitHub 인증 시)
    private final String githubId;
    private final String login;
    private final String name;
    private final String avatarUrl;
    private final String encryptedGithubToken;
    
    // 선택적 필드 (이메일 인증 후)
    private final String kutEmail;
    private final boolean emailVerified;

    @Override
    public String getSubject() {
        return githubId;
    }

    /**
     * GitHub OAuth 정보로부터 SignupToken 생성
     */
    public static SignupToken fromGithub(
        String githubId,
        String login,
        String name,
        String avatarUrl,
        String encryptedGithubToken
    ) {
        return SignupToken.builder()
            .githubId(githubId)
            .login(login)
            .name(name)
            .avatarUrl(avatarUrl)
            .encryptedGithubToken(encryptedGithubToken)
            .emailVerified(false)
            .build();
    }

    /**
     * 이메일 인증 후 토큰 갱신
     */
    public SignupToken withEmailVerified(String kutEmail) {
        return SignupToken.builder()
            .githubId(this.githubId)
            .login(this.login)
            .name(this.name)
            .avatarUrl(this.avatarUrl)
            .encryptedGithubToken(this.encryptedGithubToken)
            .kutEmail(kutEmail)
            .emailVerified(true)
            .build();
    }
}
