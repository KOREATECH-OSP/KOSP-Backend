package io.swkoreatech.kosp.domain.coffeechat.dto.response;

public record UnreadCountResponse(long count) {
    public static UnreadCountResponse of(long count) {
        return new UnreadCountResponse(count);
    }
}
