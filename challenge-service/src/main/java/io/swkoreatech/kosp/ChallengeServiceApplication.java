package io.swkoreatech.kosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 챌린지 서비스 애플리케이션의 진입점 클래스.
 *
 * <p>웹 서버 없이 동작하는 non-web 애플리케이션으로,
 * 스케줄링을 활성화하여 챌린지 평가 작업을 수행한다.
 * MongoDB 자동 설정은 제외한다.</p>
 */
@SpringBootApplication(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@EnableScheduling
public class ChallengeServiceApplication {

    /**
     * 애플리케이션을 시작한다.
     *
     * <p>웹 애플리케이션 타입을 {@link WebApplicationType#NONE}으로 설정하여
     * 웹 서버 없이 실행한다.</p>
     *
     * @param args 커맨드라인 인자
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ChallengeServiceApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
