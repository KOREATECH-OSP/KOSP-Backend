package kr.ac.koreatech.sw.kosp.global.auth.token;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.auth.annotation.TokenSpec;
import lombok.Builder;
import lombok.Getter;

/**
 * 리프레시 토큰
 */
@Getter
@Builder
@TokenSpec(TokenType.REFRESH)
public class RefreshToken extends JwtToken {

    private final Long userId;

    @Override
    public String getSubject() {
        return userId.toString();
    }

    /**
     * User ID로부터 RefreshToken 생성
     */
    public static RefreshToken from(User user) {
        return RefreshToken.builder()
            .userId(user.getId())
            .build();
    }
}
