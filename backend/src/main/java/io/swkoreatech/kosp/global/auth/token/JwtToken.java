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

/**
 * JWT 토큰의 추상 기반 클래스.
 * <p>토큰 생성({@link #toString()})과 파싱({@link #from(Class, String)}) 기능을 제공한다.
 * 하위 클래스는 {@link TokenSpec} 어노테이션으로 토큰 타입을 지정해야 한다.</p>
 */
public abstract class JwtToken {

    @JsonIgnore
    protected String value;

    /**
     * 이 토큰 클래스의 {@link TokenSpec} 어노테이션으로부터 토큰 타입을 반환한다.
     *
     * @return 토큰 타입
     * @throws IllegalStateException {@code @TokenSpec} 어노테이션이 없는 경우
     */
    public TokenType getTokenType() {
        TokenSpec spec = this.getClass().getAnnotation(TokenSpec.class);
        if (spec == null) {
            throw new IllegalStateException("Class " + this.getClass().getName() + " must have @TokenSpec annotation");
        }
        return spec.value();
    }

    /**
     * JWT의 subject 클레임으로 사용할 값을 반환한다.
     *
     * @return subject 값
     */
    public abstract String getSubject();

    /**
     * 토큰 객체를 JWT 문자열로 직렬화한다.
     * <p>한 번 생성된 JWT 문자열은 캐시되어 재사용된다.</p>
     */
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

    /**
     * JWT 문자열을 파싱하여 지정한 토큰 클래스의 인스턴스를 생성한다.
     *
     * @param <T>        토큰 타입
     * @param tokenClass 생성할 토큰 클래스
     * @param jwt        JWT 문자열
     * @return 파싱된 토큰 인스턴스
     * @throws InvalidTokenException 토큰 검증에 실패한 경우
     * @throws TokenParseException   클레임 변환에 실패한 경우
     */
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
