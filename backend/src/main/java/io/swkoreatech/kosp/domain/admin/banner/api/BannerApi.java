package io.swkoreatech.kosp.domain.admin.banner.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.admin.banner.dto.response.BannerSettingResponse;

/**
 * 배너 관리 API 인터페이스 (관리자 전용).
 * <p>배너 표시 여부를 토글하는 기능을 제공한다.</p>
 */
@Tag(name = "Admin - Banner", description = "배너 관리 API (관리자 전용)")
@RequestMapping("/v1/admin/banner")
public interface BannerApi {

    @Operation(summary = "배너 설정 토글", description = "배너 표시 여부를 토글합니다.")
    @ApiResponse(responseCode = "200", description = "토글 성공")
    @PatchMapping("/toggle")
    /**
     * 배너 표시 여부를 토글한다.
     *
     * @return 토글 후 배너 설정 응답
     */
    ResponseEntity<BannerSettingResponse> toggle();
}
