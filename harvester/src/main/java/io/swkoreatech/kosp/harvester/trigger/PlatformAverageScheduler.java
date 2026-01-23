package io.swkoreatech.kosp.harvester.trigger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.harvester.statistics.service.PlatformAverageCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformAverageScheduler {

    private final PlatformAverageCalculator calculator;

    @Scheduled(cron = "0 0 * * * *")
    public void calculatePlatformAverages() {
        log.info("Starting hourly platform average calculation");
        calculator.calculateAndSave();
        log.info("Completed hourly platform average calculation");
    }
}
