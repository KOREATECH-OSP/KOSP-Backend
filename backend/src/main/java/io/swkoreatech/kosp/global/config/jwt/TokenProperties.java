package io.swkoreatech.kosp.global.config.jwt;

import io.swkoreatech.kosp.global.auth.token.TokenType;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 (Secret Key + 만료 시간)
 */
@ConfigurationProperties(prefix = "jwt")
public record TokenProperties(
    String secretKey,
    Map<TokenType, Long> expiration
) {

}
