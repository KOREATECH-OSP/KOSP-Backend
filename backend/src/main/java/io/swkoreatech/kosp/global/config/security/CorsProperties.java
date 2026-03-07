package io.swkoreatech.kosp.global.config.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CORS 설정 프로퍼티.
 * <p>{@code cors} 접두사로 바인딩되는 설정값을 보유한다.</p>
 *
 * @param allowedOrigins 허용할 출처(Origin) 패턴 목록
 */
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
    List<String> allowedOrigins
) {

}
