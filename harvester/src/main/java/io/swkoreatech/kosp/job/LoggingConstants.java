package io.swkoreatech.kosp.job;

/**
 * Standardized log message templates for Spring Batch job execution.
 *
 * <p>This interface defines consistent log message templates used throughout the harvester
 * module to ensure uniform logging format and facilitate log aggregation and analysis.
 * All templates use SLF4J placeholder syntax ({}) for parameter substitution.
 *
 * <p>Usage pattern:
 * <pre>
 * // Job lifecycle
 * log.info(LoggingConstants.JOB_STARTED, userId);
 * log.info(LoggingConstants.JOB_FINISHED, userId, status);
 *
 * // Step execution
 * log.info(LoggingConstants.STEP_STARTED, userId, stepName);
 * log.info(LoggingConstants.STEP_METRICS, stepName, duration, read, write, skip);
 *
 * // Mining progress
 * log.info(LoggingConstants.MINING_PROGRESS, page, itemCount);
 * log.info(LoggingConstants.MINING_SUMMARY, totalMined, saved, skipped);
 * </pre>
 *
 * @see org.slf4j.Logger
 * @see org.springframework.batch.core.StepExecutionListener
 * @see org.springframework.batch.core.JobExecutionListener
 */
public interface LoggingConstants {

    /**
     * Job execution started.
     * Parameters: userId
     */
    String JOB_STARTED = "========== [User {}] JOB STARTED ==========";

    /**
     * Job execution finished with status.
     * Parameters: userId, status
     */
    String JOB_FINISHED = "========== [User {}] JOB FINISHED - {} ==========";

    /**
     * Step execution started.
     * Parameters: userId, stepName
     */
    String STEP_STARTED = ">>> [User {}] Starting step: {}";

    /**
     * Step execution finished with status.
     * Parameters: userId, stepName, status
     */
    String STEP_FINISHED = "<<< [User {}] Completed step: {} - status: {}";

    /**
     * Step execution metrics: duration, read count, write count, skip count.
     * Parameters: stepName, durationMs, readCount, writeCount, skipCount
     */
    String STEP_METRICS = "Step [{}] completed in {}ms - Read: {}, Write: {}, Skip: {}";

    /**
     * Mining progress during pagination.
     * Parameters: pageNumber, itemCount
     */
    String MINING_PROGRESS = "Fetched page {}, {} items";

    /**
     * Mining summary after completion.
     * Parameters: totalMined, savedCount, skippedCount
     */
    String MINING_SUMMARY = "Mined {} commits ({} saved, {} skipped)";
}
