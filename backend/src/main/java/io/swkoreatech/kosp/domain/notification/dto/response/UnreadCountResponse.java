package io.swkoreatech.kosp.domain.notification.dto.response;

/**
 * 읽지 않은 알림 수 응답 DTO.
 *
 * @param count 읽지 않은 알림 수
 */
public record UnreadCountResponse(
    long count
) {
    /**
     * 읽지 않은 알림 수로부터 응답을 생성한다.
     *
     * @param count 읽지 않은 알림 수
     * @return 읽지 않은 알림 수 응답
     */
    public static UnreadCountResponse from(long count) {
        return new UnreadCountResponse(count);
    }
}
