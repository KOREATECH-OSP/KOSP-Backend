package io.swkoreatech.kosp.domain.admin.season.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swkoreatech.kosp.common.season.model.Season;

/**
 * 시즌 전체 목록 응답 DTO (관리자용).
 */
public record AdminSeasonListResponse(List<AdminSeasonItem> seasons) {

    public record AdminSeasonItem(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive
    ) {
        public static AdminSeasonItem from(Season season) {
            return new AdminSeasonItem(
                season.getId(),
                season.getName(),
                season.getStartDate(),
                season.getEndDate(),
                season.isActive()
            );
        }
    }

    public static AdminSeasonListResponse from(List<Season> seasons) {
        return new AdminSeasonListResponse(
            seasons.stream().map(AdminSeasonItem::from).toList()
        );
    }
}
