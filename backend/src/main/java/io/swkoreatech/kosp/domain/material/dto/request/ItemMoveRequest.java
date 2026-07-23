package io.swkoreatech.kosp.domain.material.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 자료 이동 요청.
 *
 * @param folderId 이동할 대상 폴더 ID (필수, 본인 소유)
 */
public record ItemMoveRequest(
    @NotNull Long folderId
) {
}
