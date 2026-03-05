package io.swkoreatech.kosp.domain.challenge.initializer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도전 과제 초기화 컴포넌트.
 * 애플리케이션 시작 시 도전 과제 데이터를 초기화한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeInitializer implements ApplicationRunner {

    // private final ChallengeRepository challengeRepository;

    /** 애플리케이션 시작 시 도전 과제를 초기화한다. */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // init challenges if empty
    }
}
