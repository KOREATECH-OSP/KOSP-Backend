package io.swkoreatech.kosp.domain.search.api;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse;
import io.swkoreatech.kosp.domain.search.model.SearchFilter;

@Tag(name = "Search", description = "통합 검색 API")
@RequestMapping("/v1/search")
public interface SearchApi {

    @Operation(
        summary = "통합 검색",
        description = "키워드로 게시글, 모집글, 팀, 챌린지, 사용자를 통합 검색합니다. RSQL 필터 및 페이지네이션 지원."
    )
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping
    ResponseEntity<GlobalSearchResponse> search(
        @Parameter(description = "검색 키워드", required = true)
        @RequestParam String keyword,

        @Parameter(description = "검색 필터 (articles, recruits, teams, challenges, users). 미지정시 전체 검색")
        @RequestParam(required = false) Set<SearchFilter> filter,

        @Parameter(description = "RSQL 필터 (예: title==*test*;status==OPEN)")
        @RequestParam(required = false) String rsql,

        @Parameter(description = "페이지네이션 (page, size, sort)")
        @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );
}
