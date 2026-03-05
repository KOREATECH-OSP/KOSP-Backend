package io.swkoreatech.kosp.config;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * SLF4J MDC 컨텍스트를 워커 스레드로 전파하는 TaskDecorator.
 *
 * <p>향후 멀티스레드 Spring Batch 잡(예: 파티션 스텝) 사용 시,
 * MDC 값(jobExecutionId, stepName, userId)이 TaskExecutor가 생성한
 * 워커 스레드에서도 사용 가능하도록 보장한다.
 *
 * <p>현재 잡은 단일 스레드이므로 사용되지 않는다.
 * 활성화하려면 AsyncConfigurer 또는 TaskExecutor에 이 데코레이터를 설정한다.
 *
 * @see org.springframework.batch.core.partition.support.Partitioner
 * @see org.springframework.scheduling.annotation.AsyncConfigurer
 */
public class MdcTaskDecorator implements TaskDecorator {

    /**
     * MDC 컨텍스트를 캡처하여 워커 스레드에 전파하는 Runnable을 반환한다.
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
