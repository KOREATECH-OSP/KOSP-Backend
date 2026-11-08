package io.swkoreatech.kosp.domain.admin.season.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 시즌 생성 요청 DTO (관리자용).
 *
 * @param name      시즌 이름
 * @param startDate 시작일
 * @param endDate   종료일
 */
public record AdminSeasonCreateRequest(
    @NotBlank String name,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
