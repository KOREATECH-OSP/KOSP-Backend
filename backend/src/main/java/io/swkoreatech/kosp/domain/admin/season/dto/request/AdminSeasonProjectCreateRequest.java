package io.swkoreatech.kosp.domain.admin.season.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 관리자 시즌 프로젝트 생성 요청 DTO.
 *
 * @param name         프로젝트 이름
 * @param projectLevel 프로젝트 레벨 (1~5)
 * @param note         비고 (선택)
 */
public record AdminSeasonProjectCreateRequest(
    @NotBlank String name,
    @NotNull @Min(1) @Max(5) Integer projectLevel,
    String note
) {
}
