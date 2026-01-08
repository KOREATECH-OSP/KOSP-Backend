package kr.ac.koreatech.sw.kosp.global.auth.token;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 토큰 (ACCESS)
 * Authorization 헤더로 전송
 */
@Getter
@Builder
public class LoginToken extends JwtToken {

    private final Long userId;
    private final String kutEmail;
    private final String kutId;
    private final String name;

    @Override
    public TokenType getTokenType() {
        return TokenType.ACCESS;
    }

    @Override
    public String getSubject() {
        return userId.toString();
    }

    /**
     * User 객체로부터 LoginToken 생성
     */
    public static LoginToken from(User user) {
        return LoginToken.builder()
            .userId(user.getId())
            .kutEmail(user.getKutEmail())
            .kutId(user.getKutId())
            .name(user.getName())
            .build();
    }
}
