package kr.ac.koreatech.sw.kosp.global.config.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB 설정
    // Spring Boot Auto-configuration이 ReactiveMongoTemplate 자동 생성
}
