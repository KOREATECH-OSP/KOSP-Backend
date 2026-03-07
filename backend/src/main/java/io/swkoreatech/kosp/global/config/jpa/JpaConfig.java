package io.swkoreatech.kosp.global.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정 클래스.
 * <p>JPA Auditing을 활성화하여 엔티티의 생성/수정 시간 자동 관리를 지원한다.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

}
