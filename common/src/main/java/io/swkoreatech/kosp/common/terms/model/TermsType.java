package io.swkoreatech.kosp.common.terms.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 약관 종류.
 *
 * <p>종류마다 활성 약관은 최대 1개이며, 버전은 종류 안에서만 유일하다.</p>
 */
@Getter
@RequiredArgsConstructor
public enum TermsType {
    SERVICE("이용약관"),
    PRIVACY("개인정보 수집·이용 동의");

    private final String description;
}
