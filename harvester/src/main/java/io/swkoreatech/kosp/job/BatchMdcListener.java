package io.swkoreatech.kosp.job;

import org.slf4j.MDC;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Populates SLF4J MDC with job correlation identifiers.
 * Enables tracing all logs from a single job execution via jobExecutionId.
 * MDC keys: jobName, jobExecutionId, userId
 */
@Component
public class BatchMdcListener implements JobExecutionListener {

    private static final String JOB_NAME = "jobName";
    private static final String JOB_EXECUTION_ID = "jobExecutionId";
    private static final String USER_ID = "userId";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        MDC.put(JOB_NAME, jobExecution.getJobInstance().getJobName());
        MDC.put(JOB_EXECUTION_ID, String.valueOf(jobExecution.getJobId()));
        MDC.put(USER_ID, jobExecution.getJobParameters().getString(USER_ID));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {

        } finally {
            MDC.clear();
        }
    }
}
