package io.swkoreatech.kosp.common.season.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시즌 프로젝트 역할 열거형.
 */
@Getter
@RequiredArgsConstructor
public enum SeasonProjectRole {
    TEAM_LEAD(2.0),
    PM(1.0),
    MEMBER(0.0);

    private final double roleBonus;
}
