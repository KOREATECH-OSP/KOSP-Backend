package io.swkoreatech.kosp.common.title.model.enums;

/**
 * 칭호 지급 출처.
 */
public enum TitleGrantSource {
    /** 배치 자동 평가를 통한 시스템 지급 */
    SYSTEM,
    /** 관리자가 수동으로 지급 */
    ADMIN,
    /** 시즌 랭킹 티어 자동 동기화를 통한 지급 (티어 하락 시 시스템이 자동 회수) */
    SEASON_TIER
}
