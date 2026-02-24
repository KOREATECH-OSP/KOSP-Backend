package io.swkoreatech.kosp.global.auth.token;

import io.swkoreatech.kosp.global.auth.annotation.TokenSpec;
import io.swkoreatech.kosp.global.auth.exception.InvalidTokenException;
import io.swkoreatech.kosp.global.auth.exception.TokenParseException;
import io.swkoreatech.kosp.global.config.jwt.TokenPropertiesProvider;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public abstract class JwtToken {

    @JsonIgnore
    protected String value;

    public TokenType getTokenType() {
        TokenSpec spec = this.getClass().getAnnotation(TokenSpec.class);
        if (spec == null) {
            throw new IllegalStateException("Class " + this.getClass().getName() + " must have @TokenSpec annotation");
        }
        return spec.value();
    }

    public abstract String getSubject();

    @Override
    public String toString() {
        if (value != null) {
            return value;
        }

        Map<String, Object> claims = TokenPropertiesProvider.objectMapper()
                .convertValue(this, new TypeReference<>() {});

        value = Jwts.builder()
            .subject(getSubject())
            .claim(TokenType.CLAIM, getTokenType().toString())
            .claims(claims)
            .expiration(Date.from(Instant.now().plusMillis(getTokenType().getExpiration())))
            .signWith(TokenPropertiesProvider.secretKey())
            .compact();

        return value;
    }

    public static <T extends JwtToken> T from(Class<T> tokenClass, String jwt) {
        Claims claims = parseJwt(jwt);

        validateTokenType(tokenClass, claims);

        T token = createFromClaims(tokenClass, claims);
        token.value = jwt;

        return token;
    }

    private static Claims parseJwt(String jwt) {
        try {
            return Jwts.parser()
                .verifyWith(TokenPropertiesProvider.secretKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
        } catch (Exception e) {
            throw new InvalidTokenException("Token validation failed", e);
        }
    }

    private static <T extends JwtToken> void validateTokenType(Class<T> tokenClass, Claims claims) {
        try {
            T temp = tokenClass.getDeclaredConstructor().newInstance();
            String expectedCategory = temp.getTokenType().toString();
            String actualCategory = claims.get(TokenType.CLAIM, String.class);

            if (!expectedCategory.equalsIgnoreCase(actualCategory)) {
                throw new InvalidTokenException(
                    "Expected " + expectedCategory + " token, but got " + actualCategory
                );
            }
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            // 기본 생성자가 없으면 검증 스킵
        }
    }

    private static <T extends JwtToken> T createFromClaims(Class<T> tokenClass, Claims claims) {
        try {
            return TokenPropertiesProvider.objectMapper().convertValue(claims, tokenClass);
        } catch (IllegalArgumentException e) {
            throw new TokenParseException("Failed to create token from claims", e);
        }
    }
}
