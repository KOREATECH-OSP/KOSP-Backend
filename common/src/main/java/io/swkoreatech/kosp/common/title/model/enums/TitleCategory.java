package io.swkoreatech.kosp.common.title.model.enums;

/**
 * 칭호 카테고리.
 * <p>칭호가 어떤 활동 계열에 속하는지를 분류한다.</p>
 */
public enum TitleCategory {
    COMMIT("커밋 계열"),
    STREAK("꾸준함 계열"),
    CHALLENGE("챌린지 계열"),
    COLLABORATION("협업 계열"),
    COMMUNITY("커뮤니티 계열"),
    INFLUENCE("영향력 계열"),
    PROJECT("프로젝트 계열"),
    OPEN_SOURCE("오픈소스 계열"),
    SEASON("시즌/이벤트 계열"),
    HONOR("희소/명예 계열"),
    ATTENDANCE("출석 계열");

    private final String displayName;

    TitleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
