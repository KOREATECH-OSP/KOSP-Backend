package io.swkoreatech.kosp.domain.admin.season.dto.response;

import java.time.LocalDate;

import io.swkoreatech.kosp.common.season.model.Season;

/**
 * 현재 활성 시즌 응답 DTO (관리자용).
 *
 * @param id        시즌 ID
 * @param name      시즌 이름
 * @param startDate 시작일
 * @param endDate   종료일
 */
public record AdminCurrentSeasonResponse(
    Long id,
    String name,
    LocalDate startDate,
    LocalDate endDate
) {
    public static AdminCurrentSeasonResponse from(Season season) {
        return new AdminCurrentSeasonResponse(
            season.getId(),
            season.getName(),
            season.getStartDate(),
            season.getEndDate()
        );
    }
}
