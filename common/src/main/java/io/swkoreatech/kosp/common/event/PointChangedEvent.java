package io.swkoreatech.kosp.common.event;

public record PointChangedEvent(
    Long userId,
    Integer amount,
    String reason,
    String source,
    String messageId
) {}
