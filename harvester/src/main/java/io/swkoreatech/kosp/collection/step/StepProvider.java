package io.swkoreatech.kosp.collection.step;

import org.springframework.batch.core.Step;

public interface StepProvider {

    Step getStep();

    String getStepName();
}
