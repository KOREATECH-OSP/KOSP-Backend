package io.swkoreatech.kosp.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * Propagates SLF4J MDC context to worker threads.
 *
 * <p>For future multi-threaded Spring Batch jobs (e.g., partitioned steps),
 * this decorator ensures MDC values (jobExecutionId, stepName, userId) are
 * available in worker threads spawned by task executors.
 *
 * <p>Current job is single-threaded, so this is NOT currently used.
 * To enable: configure AsyncConfigurer or TaskExecutor with this decorator.
 *
 * @see org.springframework.batch.core.partition.support.Partitioner
 * @see org.springframework.scheduling.annotation.AsyncConfigurer
 */
public class MdcTaskDecorator implements TaskDecorator {

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
