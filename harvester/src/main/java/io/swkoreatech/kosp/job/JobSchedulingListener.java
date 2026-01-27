package io.swkoreatech.kosp.job;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobSchedulingListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long userId = jobExecution.getJobParameters().getLong("userId");
        log.info("========== [User {}] JOB STARTED ==========", userId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long userId = jobExecution.getJobParameters().getLong("userId");
        BatchStatus status = jobExecution.getStatus();
        log.info("========== [User {}] JOB FINISHED - {} ==========", userId, status);
    }
}
