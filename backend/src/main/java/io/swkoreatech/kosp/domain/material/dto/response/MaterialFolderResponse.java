package io.swkoreatech.kosp.domain.material.dto.response;

import io.swkoreatech.kosp.domain.material.model.FolderType;
import io.swkoreatech.kosp.domain.material.model.MaterialFolder;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import io.swkoreatech.kosp.domain.material.model.Visibility;

/**
 * 학습자료 폴더 응답.
 *
 * @param id            폴더 ID
 * @param parentId      상위 폴더 ID (최상위면 null)
 * @param name          폴더명
 * @param folderType    폴더 계층 구분
 * @param source        출처
 * @param visibility    공개 범위
 * @param isStartFolder 시작 폴더 여부
 * @param sortOrder     정렬 순서
 * @param itemCount     폴더 내 자료 수
 */
public record MaterialFolderResponse(
    Long id,
    Long parentId,
    String name,
    FolderType folderType,
    MaterialSource source,
    Visibility visibility,
    boolean isStartFolder,
    int sortOrder,
    long itemCount
) {

    public static MaterialFolderResponse from(MaterialFolder folder, long itemCount) {
        return new MaterialFolderResponse(
            folder.getId(),
            folder.getParent() == null ? null : folder.getParent().getId(),
            folder.getName(),
            folder.getFolderType(),
            folder.getSource(),
            folder.getVisibility(),
            folder.isStartFolder(),
            folder.getSortOrder(),
            itemCount
        );
    }
}
