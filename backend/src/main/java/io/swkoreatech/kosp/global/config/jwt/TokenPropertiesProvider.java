package io.swkoreatech.kosp.global.config.jwt;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.security.Keys;
import io.swkoreatech.kosp.global.auth.token.TokenType;

/**
 * JWT 토큰 설정을 정적으로 제공하는 컴포넌트.
 * <p>{@link TokenProperties}로부터 비밀 키와 만료 시간을 초기화하고,
 * 정적 메서드를 통해 애플리케이션 전역에서 접근할 수 있도록 한다.</p>
 */
@Component
public class TokenPropertiesProvider {

    private static SecretKey secretKey;
    private static ObjectMapper objectMapper;

    @SuppressWarnings("java:S3010")
    private TokenPropertiesProvider(
        TokenProperties properties,
        ObjectMapper objectMapper
    ) {
        TokenPropertiesProvider.secretKey = Keys.hmacShaKeyFor(properties.secretKey().getBytes());
        TokenPropertiesProvider.objectMapper = objectMapper;

        properties.expiration().forEach(TokenType::setExpiration);
    }

    /**
     * JWT 서명에 사용되는 비밀 키를 반환한다.
     *
     * @return HMAC-SHA 비밀 키
     */
    public static SecretKey secretKey() {
        return secretKey;
    }

    /**
     * 토큰 클레임 변환에 사용되는 ObjectMapper를 반환한다.
     *
     * @return ObjectMapper 인스턴스
     */
    public static ObjectMapper objectMapper() {
        return objectMapper;
    }
}
