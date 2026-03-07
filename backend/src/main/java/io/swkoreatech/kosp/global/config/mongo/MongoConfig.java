package io.swkoreatech.kosp.global.config.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB 설정 클래스.
 * <p>MongoDB Auditing을 활성화하여 도큐먼트의 생성/수정 시간 자동 관리를 지원한다.
 * Spring Boot Auto-configuration이 MongoTemplate을 자동 생성한다.</p>
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
