package io.swkoreatech.kosp.global.config.jwt;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.swkoreatech.kosp.global.auth.token.TokenType;

/**
 * JWT 토큰 설정 프로퍼티.
 * <p>{@code jwt} 접두사로 바인딩되는 비밀 키와 토큰 타입별 만료 시간 설정을 보유한다.</p>
 *
 * @param secretKey  JWT 서명에 사용할 비밀 키
 * @param expiration 토큰 타입별 만료 시간 (밀리초 단위)
 */
@ConfigurationProperties(prefix = "jwt")
public record TokenProperties(
    String secretKey,
    Map<TokenType, Long> expiration
) {

}
