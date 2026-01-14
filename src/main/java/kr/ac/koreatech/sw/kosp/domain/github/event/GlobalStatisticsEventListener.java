package kr.ac.koreatech.sw.kosp.domain.github.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.service.GlobalStatisticsCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GlobalStatisticsEventListener {

    private final GlobalStatisticsCalculator calculator;

    @Async
    @EventListener
    public void handleCalculationRequest(GlobalStatisticsCalculationRequestedEvent event) {
        log.info("Received global statistics calculation request from: {}", event.getTriggerSource());
        try {
            calculator.calculateAndSave();
            log.info("Global statistics calculation completed successfully.");
        } catch (Exception e) {
            log.error("Failed to calculate global statistics", e);
        }
    }
}
