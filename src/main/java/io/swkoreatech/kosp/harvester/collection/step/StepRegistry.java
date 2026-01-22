package io.swkoreatech.kosp.harvester.collection.step;

import java.util.Comparator;
import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StepRegistry {

    private final List<StepProvider> stepProviders;

    public StepRegistry(List<StepProvider> stepProviders) {
        this.stepProviders = initializeProviders(stepProviders);
        log.info("StepRegistry initialized with {} step providers", this.stepProviders.size());
    }

    private List<StepProvider> initializeProviders(List<StepProvider> providers) {
        if (providers == null) {
            return List.of();
        }
        return providers;
    }

    public List<Step> getOrderedSteps() {
        return stepProviders.stream()
            .sorted(Comparator.comparingInt(StepProvider::getOrder))
            .map(StepProvider::getStep)
            .toList();
    }

    public Step getFirstStep() {
        List<Step> steps = getOrderedSteps();
        if (steps.isEmpty()) {
            throw new IllegalStateException("No steps registered");
        }
        return steps.get(0);
    }

    public List<Step> getRemainingSteps() {
        List<Step> steps = getOrderedSteps();
        if (steps.size() <= 1) {
            return List.of();
        }
        return steps.subList(1, steps.size());
    }
}
