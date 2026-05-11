package io.swkoreatech.kosp.domain.admin.title.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 칭호 아이콘 URL 수정 요청.
 *
 * @param iconUrl 새 아이콘 URL (null 허용 — null이면 iconUrl 초기화)
 */
public record AdminTitleUpdateImageRequest(
    @Size(max = 500, message = "iconUrl은 500자 이내여야 합니다.")
    String iconUrl
) {}
