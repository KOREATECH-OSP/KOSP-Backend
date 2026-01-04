package kr.ac.koreatech.sw.kosp.domain.challenge.initializer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeInitializer implements ApplicationRunner {

    // private final ChallengeRepository challengeRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // init challenges if empty
    }
}
