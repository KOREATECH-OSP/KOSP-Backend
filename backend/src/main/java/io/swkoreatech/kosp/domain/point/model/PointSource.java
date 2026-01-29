package io.swkoreatech.kosp.domain.point.model;

public enum PointSource {
    ADMIN("관리자 포인트 조정"),
    CHALLENGE("챌린지 달성 보상"),
    ACTIVITY("활동 보상");

    private final String title;

    PointSource(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
