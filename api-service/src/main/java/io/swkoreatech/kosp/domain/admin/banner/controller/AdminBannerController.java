package io.swkoreatech.kosp.domain.admin.banner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.banner.api.BannerApi;
import io.swkoreatech.kosp.domain.admin.banner.dto.response.BannerSettingResponse;
import io.swkoreatech.kosp.domain.admin.banner.service.BannerService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminBannerController implements BannerApi {

    private final BannerService bannerService;

    @Override
    @Permit(name = "admin:banner:toggle", description = "배너 설정 토글")
    public ResponseEntity<BannerSettingResponse> toggle() {
        return ResponseEntity.ok(bannerService.toggle());
    }
}
