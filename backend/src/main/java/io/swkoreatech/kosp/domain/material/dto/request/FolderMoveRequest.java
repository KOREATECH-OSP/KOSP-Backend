package io.swkoreatech.kosp.domain.material.dto.request;

/**
 * 폴더 이동 요청.
 *
 * @param parentId 이동할 상위 폴더 ID. {@code null} 이면 최상위(루트)로 이동한다.
 */
public record FolderMoveRequest(
    Long parentId
) {
}
