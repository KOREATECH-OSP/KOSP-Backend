package kr.ac.koreatech.sw.kosp.domain.community.service;

import kr.ac.koreatech.sw.kosp.domain.community.dto.request.ArticleCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.dto.request.ArticleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Integer userId, ArticleCreateRequest request) {
        User user = userRepository.getById(userId);
        Article article = request.toEntity(user.getId());
        articleRepository.save(article);
        return article.getId();
    }

    @Transactional
    public void modify(Integer userId, Long articleId, ArticleUpdateRequest request) {
        Article article = findArticle(articleId);
        validateAuthor(article, userId);
        article.update(request.category(), request.title(), request.content());
    }

    @Transactional
    public void delete(Integer userId, Long articleId) {
        Article article = findArticle(articleId);
        validateAuthor(article, userId);
        articleRepository.delete(article);
    }

    @Transactional
    public ArticleResponse get(Long articleId) {
        Article article = findArticle(articleId);
        article.increaseViews();
        User author = userRepository.getById(article.getAuthorId());
        return ArticleResponse.from(article, author.getName());
    }

    public Page<ArticleListResponse> getList(String category, Pageable pageable) {
        Page<Article> articles = findArticles(category, pageable);
        return articles.map(this::toSummary);
    }

    private Article findArticle(Long articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(() -> new GlobalException("Article not found: " + articleId, HttpStatus.NOT_FOUND));
    }

    private void validateAuthor(Article article, Integer userId) {
        if (!article.getAuthorId().equals(userId)) {
            throw new GlobalException("Unauthorized access to article", HttpStatus.FORBIDDEN);
        }
    }

    private Page<Article> findArticles(String category, Pageable pageable) {
        if (category == null || category.isBlank()) {
            return articleRepository.findAll(pageable);
        }
        return articleRepository.findAllByCategory(category, pageable);
    }

    private ArticleListResponse toSummary(Article article) {
        User author = userRepository.getById(article.getAuthorId());
        return ArticleListResponse.from(article, author.getName(), "profileUrl"); // Profile URL logic needed
    }
}
