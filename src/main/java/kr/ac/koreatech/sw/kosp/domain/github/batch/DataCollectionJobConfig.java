
package kr.ac.koreatech.sw.kosp.domain.github.batch;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.transaction.PlatformTransactionManager;

import kr.ac.koreatech.sw.kosp.domain.github.exception.RateLimitExceededException;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.model.RateLimitInfo;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubDataCollectionRetryService;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubRateLimitChecker;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataCollectionJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GithubDataCollectionRetryService dataCollectionRetryService;
    private final GithubUserRepository githubUserRepository;
    private final GithubRateLimitChecker rateLimitChecker;
    private final GithubStatisticsService statisticsService;
    private final TextEncryptor textEncryptor;
    
    private static final int RATE_LIMIT_THRESHOLD = 100;
    private static final long EXTRA_WAIT_MILLIS = 1000;
    
    @Bean
    public Job dataCollectionJob(Step collectDataStep) {
        return new JobBuilder("dataCollectionJob", jobRepository)
            .start(collectDataStep)
            .build();
    }
    
    @Bean
    public Step collectDataStep() {
        return new StepBuilder("collectDataStep", jobRepository)
            .<GithubUser, GithubUser>chunk(10, transactionManager)
            .reader(githubUserReader())
            .processor(dataCollectionProcessor())
            .writer(dataCollectionWriter())
            .faultTolerant()
            .retryLimit(3)
            .retry(RateLimitExceededException.class)
            .retry(RuntimeException.class)
            .backOffPolicy(new org.springframework.retry.backoff.ExponentialBackOffPolicy())
            .skipLimit(10)
            .skip(Exception.class)
            .build();
    }
    
    @Bean
    public RepositoryItemReader<GithubUser> githubUserReader() {
        return new RepositoryItemReaderBuilder<GithubUser>()
            .name("githubUserReader")
            .repository(githubUserRepository)
            .methodName("findAll")
            .pageSize(10)
            .sorts(Map.of("githubId", Sort.Direction.ASC))
            .build();
    }
    
    @Bean
    public ItemProcessor<GithubUser, GithubUser> dataCollectionProcessor() {
        return githubUser -> {
            log.info("Processing data collection for user: {}", githubUser.getGithubLogin());
            
            String token = textEncryptor.decrypt(githubUser.getGithubToken());
            
            // Rate limit 사전 체크
            checkAndWaitIfNeeded(token, githubUser.getGithubLogin());
            
            // 데이터 수집 (Batch가 자동 retry)
            dataCollectionRetryService.collectAllData(
                githubUser.getGithubLogin(),
                token
            );
            
            // 통계 계산
            statisticsService.calculateAndSaveAllStatistics(githubUser.getGithubLogin());
            
            return githubUser;
        };
    }
    
    private void checkAndWaitIfNeeded(String token, String githubLogin) throws InterruptedException {
        RateLimitInfo info = rateLimitChecker.checkRateLimit(token);
        
        if (info.remaining() < RATE_LIMIT_THRESHOLD) {
            long waitTime = info.getWaitTimeMillis();
            log.info("Rate limit low for user {}. Remaining: {}, Waiting {} ms",
                githubLogin, info.remaining(), waitTime);
            Thread.sleep(waitTime + EXTRA_WAIT_MILLIS);
        }
    }
    
    @Bean
    public ItemWriter<GithubUser> dataCollectionWriter() {
        return items -> {
            log.info("Completed data collection for {} users", items.size());
        };
    }
}
