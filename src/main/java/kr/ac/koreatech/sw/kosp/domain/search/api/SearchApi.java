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
import kr.ac.koreatech.sw.kosp.domain.search.model.SearchSortType;

@Tag(name = "Search", description = "통합 검색 API")
@RequestMapping("/v1/search")
public interface SearchApi {

    @Operation(
        summary = "통합 검색",
        description = "키워드로 게시글, 모집글, 팀, 챌린지, 오픈소스를 통합 검색합니다. 필터와 정렬 옵션을 지정할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping
    ResponseEntity<GlobalSearchResponse> search(
        @Parameter(description = "검색 키워드", required = true)
        @RequestParam String keyword,

        @Parameter(description = "검색 필터 (articles, recruits, teams, challenges, opensource). 미지정시 전체 검색")
        @RequestParam(required = false) Set<SearchFilter> filter,

        @Parameter(description = "정렬 기준 (relevance, date_desc, date_asc). 기본값: relevance")
        @RequestParam(required = false) SearchSortType sort
    );
}
