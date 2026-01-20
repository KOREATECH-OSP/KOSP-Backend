package kr.ac.koreatech.sw.kosp.domain.search.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.search.api.SearchApi;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse;
import kr.ac.koreatech.sw.kosp.domain.search.model.SearchFilter;
import kr.ac.koreatech.sw.kosp.domain.search.service.SearchService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SearchController implements SearchApi {

    private final SearchService searchService;

    @Override
    @Permit(name = "global:search", permitAll = true, description = "통합 검색")
    public ResponseEntity<GlobalSearchResponse> search(String keyword, Set<SearchFilter> filter) {
        return ResponseEntity.ok(searchService.search(keyword, filter));
    }
}
