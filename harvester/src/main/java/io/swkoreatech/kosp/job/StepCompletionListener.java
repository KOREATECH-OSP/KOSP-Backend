package io.swkoreatech.kosp.job;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 스텝 실행 전후에 로그를 기록하는 리스너.
 *
 * <p>각 스텝의 시작과 완료 시점에 사용자 ID, 스텝 이름, 실행 상태를 로깅한다.
 */
@Slf4j
@Component
public class StepCompletionListener implements StepExecutionListener {

    /**
     * 스텝 실행 전에 시작 로그를 기록한다.
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        Long userId = stepExecution.getJobParameters().getLong("userId");
        log.info(">>> [User {}] Starting step: {}", userId, stepExecution.getStepName());
    }

    /**
     * 스텝 실행 완료 후 결과 로그를 기록한다.
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Long userId = stepExecution.getJobParameters().getLong("userId");
        log.info("<<< [User {}] Completed step: {} - status: {}", 
            userId, stepExecution.getStepName(), stepExecution.getStatus());
        return null;
    }
}
