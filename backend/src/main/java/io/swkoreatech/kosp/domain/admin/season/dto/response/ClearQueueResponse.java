package io.swkoreatech.kosp.domain.admin.season.dto.response;

/**
 * 수집 큐 비우기 결과 응답 DTO.
 *
 * @param purgedMessageCount RabbitMQ 큐에서 제거된 메시지 수
 * @param clearedJobCount    STARTED 상태에서 FAILED로 정리된 배치 잡 수
 */
public record ClearQueueResponse(
    int purgedMessageCount,
    int clearedJobCount
) {}
