package kr.ac.koreatech.sw.kosp.domain.admin.service;

import java.util.Collections;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.response.AdminSearchResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSearchService {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    public AdminSearchResponse search(String keyword, String type) {
        if (keyword == null || keyword.isBlank()) {
            return new AdminSearchResponse(Collections.emptyList(), Collections.emptyList());
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

        return new AdminSearchResponse(users, articles);
    }
}
