package kr.ac.koreatech.sw.kosp.global.auth.provider;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import kr.ac.koreatech.sw.kosp.global.auth.core.AuthToken;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthToken implements AuthToken<Claims> {

    @Getter
    private final String token;
    private final SecretKey key;

    // 생성자 1: 토큰 생성 (Create) - expireTime imply TTL (ms) ? OR Absolute Time?
    public JwtAuthToken(String id, Map<String, Object> claims, long expireTime, SecretKey key) {
        this.key = key;
        this.token = createJwtAuthToken(id, claims, expireTime);
    }

    // 생성자 2: 토큰 변환 (Convert)
    public JwtAuthToken(String token, SecretKey key) {
        this.token = token;
        this.key = key;
    }

    @Override
    public boolean validate() {
        return getData() != null;
    }

    @Override
    public Claims getData() {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    private String createJwtAuthToken(String id, Map<String, Object> claims, long expireTime) {
        var tokenBuilder = Jwts.builder()
            .subject(id)
            .signWith(key);

        if (claims != null) {
            tokenBuilder.claims(claims);
        }

        if (expireTime > 0) {
            tokenBuilder.expiration(
                Date.from(Instant.now().plusMillis(expireTime))
            );
        }

        return tokenBuilder.compact();
    }
}
