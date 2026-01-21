package kr.ac.koreatech.sw.kosp.domain.banner.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.admin.banner.dto.response.BannerSettingResponse;

@Tag(name = "Banner", description = "배너 API")
@RequestMapping("/v1/banner")
public interface BannerApi {

    @Operation(summary = "배너 설정 조회", description = "배너 표시 여부를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<BannerSettingResponse> getSetting();
}
