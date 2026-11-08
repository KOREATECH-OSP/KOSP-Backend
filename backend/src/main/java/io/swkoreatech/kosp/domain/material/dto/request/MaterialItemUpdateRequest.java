package io.swkoreatech.kosp.domain.material.dto.request;

/**
 * 자료 메타데이터 수정 요청 (이름 바꾸기 등).
 *
 * @param title        자료명 (null 이면 기존 유지)
 * @param subjectName  과목명
 * @param materialYear 연도
 * @param semester     학기
 */
public record MaterialItemUpdateRequest(
    String title,
    String subjectName,
    Integer materialYear,
    String semester
) {
}
