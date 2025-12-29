package kr.ac.koreatech.sw.kosp.global.config;

import org.springframework.context.annotation.Configuration;

@Configuration
// @EnableBatchProcessing <- Spring Batch 5.x에서는 기본적으로 불필요하거나 별도로 설정, 여기서는 기본 설정 따름
public class BatchConfig {
    // 필요한 경우 JobRepository, TransactionManager 등을 커스터마이징 가능
    // Spring Boot Starter Batch가 자동으로 구성해주므로 우선 비워둠.
}
