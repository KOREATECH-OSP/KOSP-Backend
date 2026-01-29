package io.swkoreatech.kosp.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Automatically logs step execution metrics after each step completes.
 * Extracts duration, read/write/skip counts from StepExecution.
 */
@Component
public class StepMetricsListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(StepMetricsListener.class);
    private long startTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        MDC.put("stepName", stepExecution.getStepName());
        startTime = System.currentTimeMillis();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            logMetrics(stepExecution);
            return stepExecution.getExitStatus();
        } finally {
            MDC.remove("stepName");
        }
    }

    private void logMetrics(StepExecution stepExecution) {
        long duration = calculateDuration();
        long readCount = stepExecution.getReadCount();
        long writeCount = stepExecution.getWriteCount();
        long skipCount = stepExecution.getSkipCount();

        log.info("Step [{}] completed in {}ms - Read: {}, Write: {}, Skip: {}",
            stepExecution.getStepName(), duration, readCount, writeCount, skipCount);
    }

    private long calculateDuration() {
        return System.currentTimeMillis() - startTime;
    }
}
