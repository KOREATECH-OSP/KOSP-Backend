package io.swkoreatech.kosp.domain.material.dto.request;

import io.swkoreatech.kosp.domain.material.model.FolderType;

/**
 * 학습자료 폴더 수정 요청.
 *
 * @param name       폴더명
 * @param folderType 폴더 계층 구분
 * @param sortOrder  정렬 순서
 */
public record MaterialFolderUpdateRequest(
    String name,
    FolderType folderType,
    Integer sortOrder
) {
}
