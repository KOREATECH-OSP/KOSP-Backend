package io.swkoreatech.kosp.domain.admin.search.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.search.api.AdminSearchApi;
import io.swkoreatech.kosp.domain.admin.search.dto.response.AdminSearchResponse;
import io.swkoreatech.kosp.domain.admin.search.service.AdminSearchService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 통합 검색 컨트롤러.
 * <p>{@link AdminSearchApi}를 구현하여 사용자 및 게시글 통합 검색 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminSearchController implements AdminSearchApi {

    private final AdminSearchService adminSearchService;

    @Override
    @Permit(name = "admin:search", description = "통합 검색")
    public ResponseEntity<AdminSearchResponse> search(String keyword, String type) {
        return ResponseEntity.ok(adminSearchService.search(keyword, type));
    }
}
