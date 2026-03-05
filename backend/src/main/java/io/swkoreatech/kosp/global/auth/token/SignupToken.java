package io.swkoreatech.kosp.global.auth.token;

import io.swkoreatech.kosp.global.auth.annotation.TokenSpec;

import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 과정에서 사용되는 SIGNUP 타입 JWT 토큰.
 * <p>GitHub 인증 정보(필수)와 이메일 인증 정보(선택)를 클레임으로 포함한다.</p>
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

    /** {@inheritDoc} */
    @Override
    public String getSubject() {
        return githubId;
    }

    /**
     * GitHub OAuth 인증 정보로부터 SignupToken을 생성한다.
     *
     * @param githubId              GitHub 사용자 ID
     * @param login                 GitHub 로그인 이름
     * @param name                  GitHub 표시 이름
     * @param avatarUrl             GitHub 프로필 이미지 URL
     * @param encryptedGithubToken  암호화된 GitHub 액세스 토큰
     * @return 생성된 SignupToken
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
     * 이메일 인증이 완료된 새 SignupToken을 생성한다.
     * <p>기존 GitHub 정보를 유지하면서 이메일 인증 상태를 갱신한다.</p>
     *
     * @param kutEmail 인증된 학교 이메일 주소
     * @return 이메일 인증이 완료된 새 SignupToken
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
