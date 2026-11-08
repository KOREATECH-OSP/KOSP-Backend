package io.swkoreatech.kosp.domain.admin.season.dto.request;

import java.time.LocalDate;

/**
 * 시즌 수정 요청 DTO (관리자용).
 *
 * <p>null인 필드는 기존 값을 유지한다.</p>
 *
 * @param name      시즌 이름 (null이면 유지)
 * @param startDate 시작일 (null이면 유지)
 * @param endDate   종료일 (null이면 유지)
 * @param isActive  활성 여부 (null이면 유지, true로 변경 시 기존 활성 시즌 자동 비활성화)
 */
public record AdminSeasonUpdateRequest(
    String name,
    LocalDate startDate,
    LocalDate endDate,
    Boolean isActive
) {}
