package kr.ac.koreatech.sw.kosp.domain.github.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Bean(name = "batchDataSource")
    public DataSource batchDataSource(
        @Value("${spring.batch.datasource.url}") String url,
        @Value("${spring.batch.datasource.username}") String username,
        @Value("${spring.batch.datasource.password}") String password
    ) {
        return DataSourceBuilder.create()
            .url(url)
            .username(username)
            .password(password)
            .build();
    }
}
