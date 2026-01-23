package io.swkoreatech.kosp.domain.banner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.banner.dto.response.BannerSettingResponse;
import io.swkoreatech.kosp.domain.admin.banner.service.BannerService;
import io.swkoreatech.kosp.domain.banner.api.BannerApi;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BannerController implements BannerApi {

    private final BannerService bannerService;

    @Override
    @Permit(permitAll = true, name = "banner:read", description = "배너 설정 조회")
    public ResponseEntity<BannerSettingResponse> getSetting() {
        return ResponseEntity.ok(bannerService.getSetting());
    }
}
