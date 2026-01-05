package kr.ac.koreatech.sw.kosp.domain.challenge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
    
    String imageUrl,
    
    @NotNull(message = "포인트는 필수입니다.")
    @Positive(message = "포인트는 양수여야 합니다.")
    Integer point,
    
    @NotNull(message = "최대 진행도는 필수입니다.")
    @Positive(message = "최대 진행도는 양수여야 합니다.")
    Integer maxProgress,
    
    @NotBlank(message = "진행도 필드는 필수입니다.")
    String progressField
) {
}
