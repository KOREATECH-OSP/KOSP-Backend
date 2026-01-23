package io.swkoreatech.kosp.domain.admin.search.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.search.api.AdminSearchApi;
import io.swkoreatech.kosp.domain.admin.search.dto.response.AdminSearchResponse;
import io.swkoreatech.kosp.domain.admin.search.service.AdminSearchService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

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
