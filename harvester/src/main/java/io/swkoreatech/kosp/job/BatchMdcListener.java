package io.swkoreatech.kosp.job;

import org.slf4j.MDC;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * SLF4J MDC에 잡 상관관계 식별자를 설정하는 리스너.
 *
 * <p>jobExecutionId를 통해 단일 잡 실행의 모든 로그를 추적할 수 있게 한다.
 * MDC 키: jobName, jobExecutionId, userId
 */
@Component
public class BatchMdcListener implements JobExecutionListener {

    private static final String JOB_NAME = "jobName";
    private static final String JOB_EXECUTION_ID = "jobExecutionId";
    private static final String USER_ID = "userId";

    /**
     * 잡 시작 전에 MDC에 잡 관련 식별 정보를 설정한다.
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        MDC.put(JOB_NAME, jobExecution.getJobInstance().getJobName());
        MDC.put(JOB_EXECUTION_ID, String.valueOf(jobExecution.getJobId()));
        MDC.put(USER_ID, jobExecution.getJobParameters().getString(USER_ID));
    }

    /**
     * 잡 완료 후 MDC를 정리한다.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        try {

        } finally {
            MDC.clear();
        }
    }
}
