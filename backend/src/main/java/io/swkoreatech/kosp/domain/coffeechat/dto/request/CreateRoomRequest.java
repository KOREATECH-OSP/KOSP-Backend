package io.swkoreatech.kosp.domain.coffeechat.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(
    @NotNull Long partnerId
) {}
