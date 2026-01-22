package io.swkoreatech.kosp.domain.admin.search.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.admin.search.dto.response.AdminSearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Admin - Search", description = "관리자 전용 통합 검색 API")
@RequestMapping("/v1/admin/search")
public interface AdminSearchApi {

    @Operation(summary = "통합 검색", description = "키워드로 사용자 및 게시글을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping
    ResponseEntity<AdminSearchResponse> search(
        @Parameter(description = "검색어") @RequestParam String keyword,
        @Parameter(description = "검색 유형 (USER, ARTICLE, ALL)") @RequestParam(required = false, defaultValue = "ALL") String type
    );
}
