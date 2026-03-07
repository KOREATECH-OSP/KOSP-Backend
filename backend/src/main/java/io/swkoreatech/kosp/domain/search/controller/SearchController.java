package io.swkoreatech.kosp.domain.search.controller;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.search.api.SearchApi;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse;
import io.swkoreatech.kosp.domain.search.model.SearchFilter;
import io.swkoreatech.kosp.domain.search.service.SearchService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 통합 검색 컨트롤러.
 * {@link SearchApi}의 구현체로, 통합 검색 요청을 처리한다.
 */
@RestController
@RequiredArgsConstructor
public class SearchController implements SearchApi {

    private final SearchService searchService;

    /** {@inheritDoc} */
    @Override
    @Permit(name = "global:search", permitAll = true, description = "통합 검색")
    public ResponseEntity<GlobalSearchResponse> search(
        String keyword,
        Set<SearchFilter> filter,
        String rsql,
        Pageable pageable
    ) {
        return ResponseEntity.ok(searchService.search(keyword, filter, rsql, pageable));
    }
}
