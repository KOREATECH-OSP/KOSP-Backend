package io.swkoreatech.kosp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 관련 설정 클래스.
 *
 * <p>JPA Auditing 기능을 활성화하여 엔티티의 생성/수정 시각을 자동으로 관리한다.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

}
