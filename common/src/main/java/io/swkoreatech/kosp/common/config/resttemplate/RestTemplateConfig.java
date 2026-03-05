package io.swkoreatech.kosp.common.config.resttemplate;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

/**
 * {@link RestTemplate} 설정 클래스.
 *
 * <p>HTTP 클라이언트 빈을 등록하고, Spring Retry 기능을 활성화한다.</p>
 */
@Configuration
@EnableRetry
public class RestTemplateConfig {

    /**
     * {@link RestTemplate} 빈을 생성한다.
     *
     * @param builder RestTemplate 빌더
     * @return 설정된 {@link RestTemplate} 인스턴스
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
