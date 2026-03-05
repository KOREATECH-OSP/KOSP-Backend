package io.swkoreatech.kosp.trigger;

import io.swkoreatech.kosp.statistics.service.PlatformAverageCalculator;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 플랫폼 평균 통계를 주기적으로 계산하는 스케줄러.
 *
 * <p>매시간 정각에 {@link PlatformAverageCalculator}를 호출하여
 * 플랫폼 전체 사용자의 평균 통계를 갱신한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformAverageScheduler {

    private final PlatformAverageCalculator calculator;

    /**
     * 매시간 정각에 플랫폼 평균 통계를 계산한다.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void calculatePlatformAverages() {
        log.info("Starting hourly platform average calculation");
        calculator.calculateAndSave();
        log.info("Completed hourly platform average calculation");
    }
}
