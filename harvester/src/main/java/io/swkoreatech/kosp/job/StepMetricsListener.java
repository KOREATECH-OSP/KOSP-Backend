package io.swkoreatech.kosp.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 스텝 실행 메트릭을 자동으로 로깅하는 리스너.
 *
 * <p>각 스텝 완료 후 소요 시간, 읽기/쓰기/건너뛰기 횟수를
 * StepExecution에서 추출하여 로그로 기록한다.
 */
@Component
public class StepMetricsListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(StepMetricsListener.class);
    private long startTime;

    /**
     * 스텝 시작 시 MDC에 스텝 이름을 설정하고 시작 시각을 기록한다.
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        MDC.put("stepName", stepExecution.getStepName());
        startTime = System.currentTimeMillis();
    }

    /**
     * 스텝 완료 후 메트릭을 로깅하고 MDC를 정리한다.
     */
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
