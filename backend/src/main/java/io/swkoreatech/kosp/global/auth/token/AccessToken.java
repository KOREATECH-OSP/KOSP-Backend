package io.swkoreatech.kosp.global.auth.token;

import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.global.auth.annotation.TokenSpec;

import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 인증에 사용되는 ACCESS 타입 JWT 토큰.
 * <p>사용자 ID, 이메일, 학번, 이름, 관리자 접근 가능 여부를 클레임으로 포함한다.</p>
 */
@Getter
@Builder
@TokenSpec(TokenType.ACCESS)
public class AccessToken extends JwtToken {

    private final Long userId;
    private final String kutEmail;
    private final String kutId;
    private final String name;
    private final Boolean canAccessAdmin;

    /** {@inheritDoc} */
    @Override
    public String getSubject() {
        return userId.toString();
    }

    /**
     * User 객체로부터 AccessToken을 생성한다.
     *
     * @param user 사용자 엔티티
     * @return 생성된 AccessToken
     */
    public static AccessToken from(User user) {
        boolean hasAdminAccess = user.getRoles().stream()
            .anyMatch(Role::getCanAccessAdmin);

        return AccessToken.builder()
            .userId(user.getId())
            .kutEmail(user.getKutEmail())
            .kutId(user.getKutId())
            .name(user.getName())
            .canAccessAdmin(hasAdminAccess)
            .build();
    }
}
