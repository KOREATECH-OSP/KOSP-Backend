package kr.ac.koreatech.sw.kosp.global.config.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "kr.ac.koreatech.sw.kosp.domain.github.mongo.repository")
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB 설정
    // Spring Boot Auto-configuration이 대부분 처리
}
