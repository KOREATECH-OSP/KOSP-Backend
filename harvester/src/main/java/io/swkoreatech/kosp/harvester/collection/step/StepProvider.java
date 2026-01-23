package io.swkoreatech.kosp.harvester.collection.step;

import org.springframework.batch.core.Step;

public interface StepProvider {

    Step getStep();

    String getStepName();
}
