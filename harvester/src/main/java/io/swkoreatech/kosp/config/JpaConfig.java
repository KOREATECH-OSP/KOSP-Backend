package io.swkoreatech.kosp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정 클래스.
 *
 * <p>JPA Auditing을 활성화하여 엔티티의 생성/수정 시각 자동 관리를 지원한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

}
