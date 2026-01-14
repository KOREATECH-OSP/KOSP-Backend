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
public class GithubStatisticsEventListener {

    private final GlobalStatisticsCalculator globalCalculator;
    private final kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService githubStatisticsService;

    @Async
    @EventListener
    public void handleGlobalStatisticsCalculation(GlobalStatisticsCalculationRequestedEvent event) {
        log.info("Received global statistics calculation request from: {}", event.getTriggerSource());
        try {
            globalCalculator.calculateAndSave();
            log.info("Global statistics calculation completed successfully.");
        } catch (Exception e) {
            log.error("Failed to calculate global statistics", e);
        }
    }

    @Async
    @EventListener
    public void handleUserStatisticsCalculation(UserStatisticsCalculationRequestedEvent event) {
        log.info("Received user statistics calculation request for: {}", event.getGithubLogin());
        try {
            githubStatisticsService.calculateAndSaveAllStatistics(event.getGithubLogin());
            log.info("User statistics calculation completed for: {}", event.getGithubLogin());
        } catch (Exception e) {
            log.error("Failed to calculate statistics for user: {}", event.getGithubLogin(), e);
        }
    }
}
