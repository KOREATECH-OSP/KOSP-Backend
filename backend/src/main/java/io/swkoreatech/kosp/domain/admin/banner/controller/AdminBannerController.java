package io.swkoreatech.kosp.domain.admin.banner.controller;

import io.swkoreatech.kosp.domain.admin.banner.api.BannerApi;
import io.swkoreatech.kosp.domain.admin.banner.dto.response.BannerSettingResponse;
import io.swkoreatech.kosp.domain.admin.banner.service.BannerService;
import io.swkoreatech.kosp.global.security.annotation.Permit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 배너 관리 컨트롤러 (관리자 전용).
 * <p>{@link BannerApi}를 구현하여 배너 설정 토글 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminBannerController implements BannerApi {

    private final BannerService bannerService;

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:banner:toggle", description = "배너 설정 토글")
    public ResponseEntity<BannerSettingResponse> toggle() {
        return ResponseEntity.ok(bannerService.toggle());
    }
}
