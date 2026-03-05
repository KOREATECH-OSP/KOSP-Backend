package io.swkoreatech.kosp.domain.admin.banner.dto.response;

/**
 * 배너 설정 응답 DTO.
 *
 * @param isActive 배너 활성화 여부
 */
public record BannerSettingResponse(
    boolean isActive
) {
    /**
     * 배너 활성화 상태로부터 응답 DTO를 생성한다.
     *
     * @param isActive 배너 활성화 여부
     * @return 배너 설정 응답 DTO
     */
    public static BannerSettingResponse from(boolean isActive) {
        return new BannerSettingResponse(isActive);
    }
}
