package io.swkoreatech.kosp.domain.material.model;

/**
 * 폴더 계층 구분.
 *
 * <p>연도 &gt; 학기 &gt; 과목 계층을 표현하며, 그 외 사용자 정의 폴더는 {@code CUSTOM}이다.</p>
 */
public enum FolderType {

    /** 연도 폴더 (예: 2026). */
    YEAR,

    /** 학기 폴더 (예: 1학기). */
    SEMESTER,

    /** 과목 폴더 (예: 운영체제). */
    SUBJECT,

    /** 사용자 정의 폴더. */
    CUSTOM
}
