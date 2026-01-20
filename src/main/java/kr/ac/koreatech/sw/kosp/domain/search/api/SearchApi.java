package kr.ac.koreatech.sw.kosp.domain.search.api;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse;
import kr.ac.koreatech.sw.kosp.domain.search.model.SearchFilter;

@Tag(name = "Search", description = "통합 검색 API")
@RequestMapping("/v1/search")
public interface SearchApi {

    @Operation(
        summary = "통합 검색",
        description = "키워드로 게시글, 모집글, 팀, 챌린지를 통합 검색합니다."
    )
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping
    ResponseEntity<GlobalSearchResponse> search(
        @Parameter(description = "검색 키워드", required = true)
        @RequestParam String keyword,

        @Parameter(description = "검색 필터 (articles, recruits, teams, challenges). 미지정시 전체 검색")
        @RequestParam(required = false) Set<SearchFilter> filter
    );
}
