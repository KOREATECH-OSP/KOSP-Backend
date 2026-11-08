package io.swkoreatech.kosp.domain.material.dto.response;

import java.util.List;

/**
 * 학습자료 import(반자동 수집/동기화) 결과 요약.
 *
 * @param created   신규 생성된 자료 수
 * @param updated   원본 변경으로 갱신된 자료 수
 * @param unchanged 변경 없이 동기화 시각만 갱신된 자료 수
 * @param items     처리된 자료 목록 (최종 상태)
 */
public record MaterialImportResponse(
    int created,
    int updated,
    int unchanged,
    List<MaterialItemResponse> items
) {
}
