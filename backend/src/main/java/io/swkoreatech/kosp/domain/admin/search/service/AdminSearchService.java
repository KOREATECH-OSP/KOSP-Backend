package io.swkoreatech.kosp.domain.admin.search.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.admin.search.dto.response.AdminSearchResponse;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 통합 검색 서비스.
 * <p>키워드 및 검색 유형에 따라 사용자 또는 게시글을 검색하는 비즈니스 로직을 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSearchService {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    /**
     * 키워드로 사용자 및 게시글을 통합 검색한다.
     *
     * @param keyword 검색어
     * @param type    검색 유형 (USER, ARTICLE, ALL)
     * @return 통합 검색 응답 DTO
     */
    public AdminSearchResponse search(String keyword, String type) {
        if (keyword == null || keyword.isBlank()) {
            return AdminSearchResponse.empty();
        }

        List<AdminSearchResponse.UserSummary> users = Collections.emptyList();
        List<AdminSearchResponse.ArticleSummary> articles = Collections.emptyList();

        if ("USER".equalsIgnoreCase(type) || "ALL".equalsIgnoreCase(type) || type == null) {
            users = userRepository.findByNameContaining(keyword).stream()
                .map(AdminSearchResponse.UserSummary::from)
                .toList();
        }

        if ("ARTICLE".equalsIgnoreCase(type) || "ALL".equalsIgnoreCase(type) || type == null) {
            articles = articleRepository.findByTitleContaining(keyword).stream()
                .map(AdminSearchResponse.ArticleSummary::from)
                .toList();
        }

        return AdminSearchResponse.from(users, articles);
    }
}
