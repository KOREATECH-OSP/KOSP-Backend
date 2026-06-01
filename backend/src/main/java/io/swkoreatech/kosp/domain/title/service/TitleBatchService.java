package io.swkoreatech.kosp.domain.title.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.title.model.TitleBatchLog;
import io.swkoreatech.kosp.common.title.repository.TitleBatchLogRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 칭호 평가 배치 서비스.
 *
 * <p>매일 새벽 3시에 모든 활성 유저를 대상으로 칭호 조건을 평가하고 칭호를 지급한다.
 * 실행 이력은 {@link TitleBatchLog}에 기록된다.</p>
 *
 * <p>배치 실행 흐름:</p>
 * <ol>
 *   <li>TitleBatchLog 생성 (RUNNING)</li>
 *   <li>전체 활성 유저 로드</li>
 *   <li>TitleEvaluationService.evaluateAll() 호출</li>
 *   <li>TitleBatchLog 업데이트 (SUCCESS / FAILED)</li>
 * </ol>
 *
 * <p>TODO: 유저 수가 많아지면 페이징 처리로 전환 필요.
 * 현재는 findAllByIsDeletedFalse()로 전체 로드 방식 사용.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TitleBatchService {

    private final UserRepository userRepository;
    private final TitleEvaluationService titleEvaluationService;
    private final TitleBatchLogRepository titleBatchLogRepository;

    /**
     * 칭호 평가 배치를 실행한다.
     * 매일 새벽 3시 실행. 필요 시 Admin API를 통해 수동 트리거 가능.
     *
     * <p>cron 표현식: 초 분 시 일 월 요일</p>
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void runTitleEvaluationBatch() {
        log.info("[TitleBatch] 칭호 평가 배치 시작");
        TitleBatchLog batchLog = createBatchLog();

        try {
            List<User> activeUsers = userRepository.findAllByIsDeletedFalse();
            log.info("[TitleBatch] 평가 대상 유저 수: {}", activeUsers.size());

            int grantedCount = titleEvaluationService.evaluateAll(activeUsers);

            completeBatchLog(batchLog, activeUsers.size(), grantedCount);
            log.info("[TitleBatch] 배치 완료. 처리 유저={}, 지급 칭호={}", activeUsers.size(), grantedCount);

        } catch (Exception e) {
            failBatchLog(batchLog, e.getMessage());
            log.error("[TitleBatch] 배치 실행 중 치명적 오류 발생. batchLogId={}", batchLog.getId(), e);
        }
    }

    @Transactional
    public TitleBatchLog createBatchLog() {
        TitleBatchLog log = TitleBatchLog.builder()
            .executedAt(LocalDateTime.now())
            .build();
        return titleBatchLogRepository.save(log);
    }

    @Transactional
    public void completeBatchLog(TitleBatchLog batchLog, int processedUserCount, int grantedTitleCount) {
        batchLog.complete(processedUserCount, grantedTitleCount);
        titleBatchLogRepository.save(batchLog);
    }

    @Transactional
    public void failBatchLog(TitleBatchLog batchLog, String errorMessage) {
        batchLog.fail(errorMessage);
        titleBatchLogRepository.save(batchLog);
    }
}
