package io.swkoreatech.kosp.domain.banner.api;

import io.swkoreatech.kosp.domain.admin.banner.dto.response.BannerSettingResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 배너 관련 API 인터페이스.
 * 배너 표시 설정을 조회하는 엔드포인트를 정의한다.
 */
@Tag(name = "Banner", description = "배너 API")
@RequestMapping("/v1/banner")
public interface BannerApi {

    /**
     * 배너 설정을 조회한다.
     *
     * @return 배너 설정 응답
     */
    @Operation(summary = "배너 설정 조회", description = "배너 표시 여부를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<BannerSettingResponse> getSetting();
}
