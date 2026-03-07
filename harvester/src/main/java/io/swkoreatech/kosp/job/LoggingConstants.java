package io.swkoreatech.kosp.job;

/**
 * Spring Batch 잡 실행을 위한 표준화된 로그 메시지 템플릿 인터페이스.
 *
 * <p>harvester 모듈 전체에서 일관된 로그 형식을 보장하고
 * 로그 집계 및 분석을 용이하게 하기 위한 로그 메시지 템플릿을 정의한다.
 * 모든 템플릿은 SLF4J 플레이스홀더 구문({@code {}})을 사용한다.
 *
 * <p>사용 예시:
 * <pre>
 * // 잡 생명주기
 * log.info(LoggingConstants.JOB_STARTED, userId);
 * log.info(LoggingConstants.JOB_FINISHED, userId, status);
 *
 * // 스텝 실행
 * log.info(LoggingConstants.STEP_STARTED, userId, stepName);
 * log.info(LoggingConstants.STEP_METRICS, stepName, duration, read, write, skip);
 *
 * // 마이닝 진행
 * log.info(LoggingConstants.MINING_PROGRESS, page, itemCount);
 * log.info(LoggingConstants.MINING_SUMMARY, totalMined, saved, skipped);
 * </pre>
 *
 * @see org.slf4j.Logger
 * @see org.springframework.batch.core.StepExecutionListener
 * @see org.springframework.batch.core.JobExecutionListener
 */
public interface LoggingConstants {

    /** 잡 실행 시작. 파라미터: userId */
    String JOB_STARTED = "========== [User {}] JOB STARTED ==========";

    /** 잡 실행 완료 및 상태. 파라미터: userId, status */
    String JOB_FINISHED = "========== [User {}] JOB FINISHED - {} ==========";

    /** 스텝 실행 시작. 파라미터: userId, stepName */
    String STEP_STARTED = ">>> [User {}] Starting step: {}";

    /** 스텝 실행 완료 및 상태. 파라미터: userId, stepName, status */
    String STEP_FINISHED = "<<< [User {}] Completed step: {} - status: {}";

    /** 스텝 실행 메트릭: 소요시간, 읽기/쓰기/건너뛰기 수. 파라미터: stepName, durationMs, readCount, writeCount, skipCount */
    String STEP_METRICS = "Step [{}] completed in {}ms - Read: {}, Write: {}, Skip: {}";

    /** 마이닝 진행 상황. 파라미터: pageNumber, itemCount */
    String MINING_PROGRESS = "Fetched page {}, {} items";

    /** 마이닝 완료 요약. 파라미터: totalMined, savedCount, skippedCount */
    String MINING_SUMMARY = "Mined {} commits ({} saved, {} skipped)";
}
