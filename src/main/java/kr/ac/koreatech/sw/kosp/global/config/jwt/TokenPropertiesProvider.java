package kr.ac.koreatech.sw.kosp.global.config.jwt;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.security.Keys;
import kr.ac.koreatech.sw.kosp.global.auth.token.TokenType;

@Component
public class TokenPropertiesProvider {

    private static SecretKey secretKey;

    @SuppressWarnings("java:S3010")
    private TokenPropertiesProvider(
        TokenProperties properties
    ) {
        secretKey = Keys.hmacShaKeyFor(properties.secretKey().getBytes());

        properties.expiration().forEach(TokenType::setExpiration);
    }

    public static SecretKey secretKey() {
        return secretKey;
    }
}
