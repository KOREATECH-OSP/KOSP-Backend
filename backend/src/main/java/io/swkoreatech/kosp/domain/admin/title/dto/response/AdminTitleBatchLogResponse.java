package io.swkoreatech.kosp.domain.admin.title.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.title.model.TitleBatchLog;

/**
 * 배치 실행 이력 단건 응답 DTO.
 *
 * @param id                  배치 로그 ID
 * @param executedAt          배치 시작 시각
 * @param finishedAt          배치 종료 시각 (진행 중이면 null)
 * @param status              상태 (RUNNING / SUCCESS / FAILED)
 * @param processedUserCount  처리한 유저 수
 * @param grantedTitleCount   지급한 칭호 수
 * @param errorMessage        오류 메시지 (FAILED인 경우)
 */
public record AdminTitleBatchLogResponse(
    Long id,
    LocalDateTime executedAt,
    LocalDateTime finishedAt,
    String status,
    int processedUserCount,
    int grantedTitleCount,
    String errorMessage
) {
    public static AdminTitleBatchLogResponse from(TitleBatchLog log) {
        return new AdminTitleBatchLogResponse(
            log.getId(),
            log.getExecutedAt(),
            log.getFinishedAt(),
            log.getStatus().name(),
            log.getProcessedUserCount(),
            log.getGrantedTitleCount(),
            log.getErrorMessage()
        );
    }
}
