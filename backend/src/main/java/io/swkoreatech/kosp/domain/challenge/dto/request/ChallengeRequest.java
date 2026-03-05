package io.swkoreatech.kosp.domain.challenge.dto.request;

import io.swkoreatech.kosp.common.challenge.model.ImageResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 도전 과제 생성/수정 요청 DTO.
 *
 * @param name 챌린지 이름
 * @param description 챌린지 설명
 * @param condition SpEL 조건식
 * @param tier 챌린지 티어
 * @param imageResource 이미지 리소스 경로
 * @param imageResourceType 이미지 리소스 타입
 * @param point 보상 포인트
 */
public record ChallengeRequest(
    @NotBlank(message = "챌린지 이름은 필수입니다.")
    String name,

    @NotBlank(message = "챌린지 설명은 필수입니다.")
    String description,

    @NotBlank(message = "조건식은 필수입니다.")
    String condition,

    @NotNull(message = "티어는 필수입니다.")
    @Positive(message = "티어는 양수여야 합니다.")
    Integer tier,

    String imageResource,

    ImageResourceType imageResourceType,

    @NotNull(message = "포인트는 필수입니다.")
    @Positive(message = "포인트는 양수여야 합니다.")
    Integer point
) {
}
