package io.swkoreatech.kosp.domain.terms.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.terms.dto.request.AgreeTermsRequest;
import io.swkoreatech.kosp.domain.terms.dto.response.TermsResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 이용약관 API 인터페이스.
 */
@Tag(name = "Terms", description = "이용약관 API")
@RequestMapping("/v1/terms")
public interface TermsApi {

    @Operation(summary = "현재 약관 조회", description = "현재 유효한 이용약관을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "활성 약관 없음")
    @GetMapping
    ResponseEntity<TermsResponse> getActiveTerms();

    @Operation(summary = "약관 동의", description = "지정한 버전의 이용약관에 동의합니다. 이미 동의한 경우 무시됩니다.")
    @ApiResponse(responseCode = "200", description = "동의 성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "404", description = "약관 버전 없음")
    @PostMapping("/agree")
    ResponseEntity<Void> agreeTerms(@AuthUser User user, @Valid @RequestBody AgreeTermsRequest request);
}
