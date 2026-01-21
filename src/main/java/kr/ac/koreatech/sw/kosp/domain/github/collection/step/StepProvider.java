package kr.ac.koreatech.sw.kosp.domain.github.collection.step;

import org.springframework.batch.core.Step;

public interface StepProvider {

    Step getStep();

    String getStepName();

    int getOrder();
}
