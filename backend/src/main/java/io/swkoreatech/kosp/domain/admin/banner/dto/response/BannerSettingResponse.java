package io.swkoreatech.kosp.domain.admin.banner.dto.response;

public record BannerSettingResponse(
    boolean isActive
) {
    public static BannerSettingResponse from(boolean isActive) {
        return new BannerSettingResponse(isActive);
    }
}
