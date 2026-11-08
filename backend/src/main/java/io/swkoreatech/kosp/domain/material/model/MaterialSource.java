package io.swkoreatech.kosp.domain.material.model;

/**
 * 학습자료 출처 구분.
 *
 * <p>과제와 EL 자료는 별도 테이블로 분리하지 않고 이 값으로 카테고리를 구분한다.</p>
 */
public enum MaterialSource {

    /** 아우누리 온라인교육 과제. */
    AUNURI_ASSIGNMENT,

    /** 아우누리 온라인교육 EL(e-Learning) 자료. */
    AUNURI_EL,

    /** GitHub 프로젝트에서 가져온 자료. */
    GITHUB,

    /** 사용자가 직접 업로드한 자료. */
    MANUAL
}
