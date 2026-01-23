package io.swkoreatech.kosp.job;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StepCompletionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        Long userId = stepExecution.getJobParameters().getLong("userId");
        log.info(">>> [User {}] Starting step: {}", userId, stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Long userId = stepExecution.getJobParameters().getLong("userId");
        log.info("<<< [User {}] Completed step: {} - status: {}", 
            userId, stepExecution.getStepName(), stepExecution.getStatus());
        return null;
    }
}
