package io.swkoreatech.kosp.domain.material.dto.request;

import io.swkoreatech.kosp.domain.material.model.FolderType;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import io.swkoreatech.kosp.domain.material.model.Visibility;
import jakarta.validation.constraints.NotBlank;

/**
 * 학습자료 폴더 생성 요청.
 *
 * @param name        폴더명 (예: "2026", "1학기", "운영체제")
 * @param parentId    상위 폴더 ID (최상위면 null)
 * @param folderType  폴더 계층 구분 (null 이면 CUSTOM)
 * @param source      출처 (null 이면 MANUAL)
 * @param visibility  공개 범위 (null 이면 PRIVATE)
 * @param sortOrder   정렬 순서
 */
public record MaterialFolderCreateRequest(
    @NotBlank String name,
    Long parentId,
    FolderType folderType,
    MaterialSource source,
    Visibility visibility,
    Integer sortOrder
) {
}
