package kr.ac.koreatech.sw.kosp.global.auth.token;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.auth.annotation.TokenSpec;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 토큰 (ACCESS)
 */
@Getter
@Builder
@TokenSpec(TokenType.ACCESS)
public class AccessToken extends JwtToken {

    private final Long userId;
    private final String kutEmail;
    private final String kutId;
    private final String name;

    @Override
    public String getSubject() {
        return userId.toString();
    }

    /**
     * User 객체로부터 LoginToken 생성
     */
    public static AccessToken from(User user) {
        return AccessToken.builder()
            .userId(user.getId())
            .kutEmail(user.getKutEmail())
            .kutId(user.getKutId())
            .name(user.getName())
            .build();
    }
}
