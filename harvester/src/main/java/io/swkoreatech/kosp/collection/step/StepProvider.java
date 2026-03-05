package io.swkoreatech.kosp.collection.step;

import org.springframework.batch.core.Step;

/**
 * Spring Batch Step을 제공하는 인터페이스.
 *
 * <p>각 수집 단계(Step)는 이 인터페이스를 구현하여
 * Step 인스턴스와 Step 이름을 제공한다.
 */
public interface StepProvider {

    /**
     * Spring Batch Step 인스턴스를 반환한다.
     *
     * @return 구성된 Step 인스턴스
     */
    Step getStep();

    /**
     * Step의 이름을 반환한다.
     *
     * @return Step 이름 문자열
     */
    String getStepName();
}
