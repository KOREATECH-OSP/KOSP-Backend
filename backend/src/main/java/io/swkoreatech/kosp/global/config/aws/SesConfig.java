package io.swkoreatech.kosp.global.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * AWS SES(Simple Email Service) 클라이언트 설정 클래스.
 * <p>AWS 인증 정보와 리전을 사용하여 SES 클라이언트를 구성한다.</p>
 */
@Configuration
public class SesConfig {

    @Value("${aws.access-key}")
    private String accessKey;
    @Value("${aws.secret-key}")
    private String secretKey;
    @Value("${aws.region}")
    private String region;

    /**
     * AWS SES 클라이언트 빈을 생성한다.
     *
     * @return SES 클라이언트 인스턴스
     */
    @Bean
    public SesClient sesClient() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return SesClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .build();
    }

}
