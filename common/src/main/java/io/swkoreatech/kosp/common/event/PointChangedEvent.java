package io.swkoreatech.kosp.common.event;

/**
 * 포인트 변경 이벤트.
 *
 * <p>사용자의 포인트가 증가하거나 감소할 때 발행되며,
 * 포인트 이력 기록 등의 후속 처리를 트리거한다.</p>
 *
 * @param userId    포인트가 변경된 사용자 ID
 * @param amount    변경 포인트 금액 (양수: 지급, 음수: 차감)
 * @param reason    포인트 변경 사유
 * @param source    포인트 변경 출처
 * @param messageId 멱등성 보장을 위한 메시지 ID
 */
public record PointChangedEvent(
    Long userId,
    Integer amount,
    String reason,
    String source,
    String messageId
) {
}
