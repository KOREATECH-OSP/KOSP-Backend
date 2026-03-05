package io.swkoreatech.kosp.global.auth.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import io.swkoreatech.kosp.global.auth.exception.InvalidTokenException;
import io.swkoreatech.kosp.global.auth.token.RefreshToken;
import io.swkoreatech.kosp.global.auth.token.TokenType;
import lombok.RequiredArgsConstructor;

/**
 * RefreshToken을 Redis에 저장하고 관리하는 저장소.
 * <p>토큰의 저장, 존재 여부 검증, 삭제 기능을 제공한다.</p>
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    /**
     * RefreshToken을 Redis에 저장한다.
     * <p>만료 시간이 설정된 상태로 저장된다.</p>
     *
     * @param token 저장할 RefreshToken
     */
    public void save(RefreshToken token) {
        redisTemplate.opsForValue().set(
            getKey(token.getUserId()),
            token.toString(),
            TokenType.REFRESH.getExpiration(),
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Redis에 저장된 RefreshToken과 일치하는지 검증한다.
     *
     * @param token 검증할 RefreshToken
     * @throws InvalidTokenException 토큰이 존재하지 않거나 일치하지 않는 경우
     */
    public void verifyExists(RefreshToken token) {
        String storedToken = redisTemplate.opsForValue().get(getKey(token.getUserId()));

        if (storedToken == null || !storedToken.equals(token.toString())) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
    }

    /**
     * Redis에서 RefreshToken을 삭제한다.
     * <p>로그아웃 시 호출된다.</p>
     *
     * @param token 삭제할 RefreshToken
     */
    public void delete(RefreshToken token) {
        redisTemplate.delete(getKey(token.getUserId()));
    }

    private String getKey(Long userId) {
        return "refresh:" + userId;
    }
}
