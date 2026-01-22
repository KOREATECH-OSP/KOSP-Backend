package io.swkoreatech.kosp.global.config.jwt;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.security.Keys;
import io.swkoreatech.kosp.global.auth.token.TokenType;

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

    public static SecretKey secretKey() {
        return secretKey;
    }

    public static ObjectMapper objectMapper() {
        return objectMapper;
    }
}
