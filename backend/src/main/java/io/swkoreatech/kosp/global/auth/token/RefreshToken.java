package io.swkoreatech.kosp.global.auth.token;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.global.auth.annotation.TokenSpec;
import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 재발급에 사용되는 REFRESH 타입 JWT 토큰.
 * <p>사용자 ID를 클레임으로 포함한다.</p>
 */
@Getter
@Builder
@TokenSpec(TokenType.REFRESH)
public class RefreshToken extends JwtToken {

    private final Long userId;

    /** {@inheritDoc} */
    @Override
    public String getSubject() {
        return userId.toString();
    }

    /**
     * User 객체로부터 RefreshToken을 생성한다.
     *
     * @param user 사용자 엔티티
     * @return 생성된 RefreshToken
     */
    public static RefreshToken from(User user) {
        return RefreshToken.builder()
            .userId(user.getId())
            .build();
    }
}
