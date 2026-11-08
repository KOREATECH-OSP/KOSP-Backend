package io.swkoreatech.kosp.domain.resume.dto.request;

import io.swkoreatech.kosp.domain.material.model.Visibility;

/**
 * 이력서 자동 프로젝트 수정 요청.
 *
 * <p>과제/EL 자료에서 투영된 자동 프로젝트를 사용자가 덮어쓴다.
 * 텍스트 필드(name/period/summary/docLink)는 overrides 로 저장되어 원본 위에 반영되며,
 * 이후 원본 재동기화가 사용자 수정본을 덮어쓰지 않는다({@code userEdited=true}).
 * {@code visibility} 는 프로젝트 단위 공개 override 이다(기본 비공개).</p>
 *
 * <p>null 필드는 "수정 없음"이 아니라 "해당 override 해제(원본 값 사용)"로 처리한다.</p>
 */
public record AutoProjectUpdateRequest(
    String name,
    String period,
    String summary,
    String docLink,
    Visibility visibility
) {
}
