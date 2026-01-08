package kr.ac.koreatech.sw.kosp.global.auth.token;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import kr.ac.koreatech.sw.kosp.global.auth.exception.InvalidTokenException;
import kr.ac.koreatech.sw.kosp.global.auth.exception.TokenParseException;
import kr.ac.koreatech.sw.kosp.global.config.jwt.TokenPropertiesProvider;

/**
 * JWT 토큰 추상 클래스
 * DTO처럼 필드만 선언하면 자동으로 JWT 변환
 */
@Component
public abstract class JwtToken {

    protected String value;

    /**
     * 토큰 타입 (하위 클래스에서 구현)
     */
    public abstract TokenType getTokenType();

    /**
     * Subject (하위 클래스에서 구현)
     */
    public abstract String getSubject();

    /**
     * JWT 문자열로 변환
     */
    @Override
    public String toString() {
        if (value != null) {
            return value;
        }

        Map<String, Object> claims = fieldsToMap();

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
     * JWT 문자열에서 토큰 객체 생성
     */
    public static <T extends JwtToken> T from(Class<T> tokenClass, String jwt) {
        // Level 1: JWT 검증
        Claims claims = parseJwt(jwt);

        // Level 2: Category 검증
        validateTokenType(tokenClass, claims);

        // Level 3: 객체 생성
        T token = createFromClaims(tokenClass, claims);
        token.value = jwt;

        return token;
    }

    /**
     * 필드 → Map 변환 (Reflection)
     */
    @SuppressWarnings("java:S3011")  // Reflection accessibility is required
    private Map<String, Object> fieldsToMap() {
        Map<String, Object> map = new HashMap<>();

        for (Field field : getClass().getDeclaredFields()) {
            if (shouldSkipField(field)) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object fieldValue = field.get(this);
                if (fieldValue != null) {
                    map.put(field.getName(), fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new TokenParseException("Failed to serialize value: " + field.getName(), e);
            }
        }

        return map;
    }

    private boolean shouldSkipField(Field field) {
        return field.getName().equals("value") ||
            Modifier.isStatic(field.getModifiers()) ||
            Modifier.isTransient(field.getModifiers());
    }

    /**
     * JWT 파싱 및 검증
     */
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

    /**
     * 토큰 타입 검증
     */
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

    /**
     * Claims → 객체 생성 (Reflection)
     */
    @SuppressWarnings("java:S3011")  // Reflection accessibility is required
    private static <T extends JwtToken> T createFromClaims(Class<T> tokenClass, Claims claims) {
        try {
            // 필드 수집
            List<Field> fields = Arrays.stream(tokenClass.getDeclaredFields())
                .filter(f -> !f.getName().equals("value"))
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .filter(f -> !Modifier.isTransient(f.getModifiers()))
                .toList();

            // 생성자 찾기
            Class<?>[] paramTypes = fields.stream()
                .map(Field::getType)
                .toArray(Class<?>[]::new);

            Constructor<T> constructor = tokenClass.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);

            // 파라미터 값 추출
            Object[] args = fields.stream()
                .map(f -> extractClaimValue(claims, f))
                .toArray();

            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new TokenParseException("Failed to create token from claims", e);
        }
    }

    /**
     * Claim 값 추출 (타입 변환)
     */
    private static Object extractClaimValue(Claims claims, Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        Object value = claims.get(fieldName);

        if (value == null) {
            return null;
        }

        // Long 타입 특별 처리
        if (fieldType == Long.class || fieldType == long.class) {
            if (value instanceof Integer integer) {
                return integer.longValue();
            }
            if (value instanceof String string) {
                return Long.parseLong(string);
            }
        }

        return claims.get(fieldName, fieldType);
    }
}
