package kr.ac.koreatech.sw.kosp.global.auth.provider;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.auth.core.AuthToken;
import kr.ac.koreatech.sw.kosp.global.auth.model.AuthTokenCategory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoginTokenProvider extends JwtAuthTokenProvider {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration-time.access-token}")
    private long accessExpiration;

    @Value("${jwt.expiration-time.refresh-token}")
    private long refreshExpiration;

    public LoginTokenProvider(
        @Value("${jwt.secret}") String secret,
        StringRedisTemplate redisTemplate
    ) {
        super(secret);
        this.redisTemplate = redisTemplate;
    }

    /**
     * Access Token 생성 (JWS)
     */
    public AuthToken<Claims> createAccessToken(User user) {
        Map<String, Object> claims = Map.of(
            "category", AuthTokenCategory.LOGIN.getValue(),
            "kutEmail", user.getKutEmail(),
            "kutId", user.getKutId(),
            "name", user.getName()
        );
        return createAuthToken(user.getId().toString(), claims, accessExpiration);
    }

    /**
     * Refresh Token 생성 및 Redis 저장 (JWS)
     */
    public AuthToken<Claims> createRefreshToken(User user) {
        Map<String, Object> claims = Map.of(
            "category", AuthTokenCategory.LOGIN.getValue()
        );
        AuthToken<Claims> token = createAuthToken(user.getId().toString(), claims, refreshExpiration);
        
        // Redis에 Refresh Token 저장
        if (token instanceof JwtAuthToken jwtToken) {
            saveRefreshToken(user.getId(), jwtToken.getToken(), refreshExpiration);
        }
        
        return token;
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    public AuthToken<Claims> reissueAccessToken(User user) {
        return createAccessToken(user);
    }

    /**
     * Logout - Refresh Token 삭제
     */
    public void revokeRefreshToken(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    private void saveRefreshToken(Long userId, String refreshToken, long ttlMs) {
        redisTemplate.opsForValue().set(
            "refresh:" + userId,
            refreshToken,
            ttlMs,
            java.util.concurrent.TimeUnit.MILLISECONDS
        );
    }
}
