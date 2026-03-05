package io.swkoreatech.kosp.global.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 클라이언트 설정 클래스.
 * <p>AWS 인증 정보와 리전을 사용하여 S3 클라이언트와 Presigner를 구성한다.
 * 인증 정보가 없는 경우 기본 자격 증명 체인을 사용한다.</p>
 */
@Configuration
public class S3Config {

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    /**
     * AWS S3 클라이언트 빈을 생성한다.
     *
     * @return S3 클라이언트 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            return S3Client.builder()
                .region(Region.of(region))
                .build();
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }

    /**
     * AWS S3 Presigner 빈을 생성한다.
     * <p>사전 서명된(pre-signed) URL 생성에 사용된다.</p>
     *
     * @return S3 Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            return S3Presigner.builder()
                .region(Region.of(region))
                .build();
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }
}
