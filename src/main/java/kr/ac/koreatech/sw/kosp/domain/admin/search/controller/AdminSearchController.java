package kr.ac.koreatech.sw.kosp.domain.admin.search.controller;

import kr.ac.koreatech.sw.kosp.domain.admin.search.api.AdminSearchApi;
import kr.ac.koreatech.sw.kosp.domain.admin.search.dto.response.AdminSearchResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.service.AdminSearchService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
