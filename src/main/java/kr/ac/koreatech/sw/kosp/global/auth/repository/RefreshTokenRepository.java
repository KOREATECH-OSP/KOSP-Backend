package kr.ac.koreatech.sw.kosp.global.auth.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import kr.ac.koreatech.sw.kosp.global.auth.exception.InvalidTokenException;
import kr.ac.koreatech.sw.kosp.global.auth.token.RefreshToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.TokenType;
import lombok.RequiredArgsConstructor;

/**
 * RefreshToken Redis 저장소
 * Level 3 검증: 비즈니스 로직
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * RefreshToken 저장
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
     * ✅ Level 3: Redis 검증 (비즈니스 로직)
     */
    public void verifyExists(RefreshToken token) {
        String storedToken = redisTemplate.opsForValue().get(getKey(token.getUserId()));
        
        if (storedToken == null || !storedToken.equals(token.toString())) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
    }
    
    /**
     * RefreshToken 삭제 (로그아웃)
     */
    public void delete(RefreshToken token) {
        redisTemplate.delete(getKey(token.getUserId()));
    }
    
    private String getKey(Long userId) {
        return "refresh:" + userId;
    }
}
