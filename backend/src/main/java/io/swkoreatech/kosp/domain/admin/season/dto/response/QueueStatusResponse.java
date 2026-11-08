package io.swkoreatech.kosp.domain.admin.season.dto.response;

/**
 * 수집 큐 상태 응답 DTO.
 *
 * @param pendingCount 현재 큐에 대기 중인 메시지 수
 */
public record QueueStatusResponse(int pendingCount) {}
