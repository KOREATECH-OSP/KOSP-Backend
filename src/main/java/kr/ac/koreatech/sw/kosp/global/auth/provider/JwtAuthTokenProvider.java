package kr.ac.koreatech.sw.kosp.global.auth.provider;

import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.security.Keys;
import kr.ac.koreatech.sw.kosp.global.auth.core.AuthTokenProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthTokenProvider implements AuthTokenProvider<JwtAuthToken> {

    private final SecretKey key;

    public JwtAuthTokenProvider(
        @Value("${jwt.secret}") String secret
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public JwtAuthToken createAuthToken(String id, Map<String, Object> claims, long expireTime) {
        return new JwtAuthToken(id, claims, expireTime, key);
    }

    @Override
    public JwtAuthToken convertAuthToken(String token) {
        return new JwtAuthToken(token, key);
    }
}
