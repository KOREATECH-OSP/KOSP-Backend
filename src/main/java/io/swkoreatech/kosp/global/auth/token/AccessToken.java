package io.swkoreatech.kosp.global.auth.token;

import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.auth.annotation.TokenSpec;
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
    private final Boolean canAccessAdmin;

    @Override
    public String getSubject() {
        return userId.toString();
    }

    /**
     * User 객체로부터 LoginToken 생성
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
