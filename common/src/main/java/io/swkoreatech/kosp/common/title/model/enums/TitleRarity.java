package io.swkoreatech.kosp.common.title.model.enums;

/**
 * 칭호 희소도.
 * <p>칭호 획득 난이도와 희귀성을 나타낸다.</p>
 */
public enum TitleRarity {
    COMMON("일반"),
    RARE("희귀"),
    EPIC("영웅"),
    LEGENDARY("전설");

    private final String displayName;

    TitleRarity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
