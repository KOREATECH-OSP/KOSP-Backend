package io.swkoreatech.kosp.domain.admin.challenge.dto;

public record AdminChallengeResponse(
    Long id,
    String name,
    String description,
    String condition,
    Integer tier,
    String imageUrl,
    Integer point,
    Integer maxProgress,
    String progressField
) {
}
