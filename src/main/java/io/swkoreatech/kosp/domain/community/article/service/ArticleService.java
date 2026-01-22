package io.swkoreatech.kosp.domain.community.article.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.community.article.dto.response.AdminArticleResponse;
import io.swkoreatech.kosp.domain.community.article.dto.request.ArticleRequest;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleResponse;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.model.ArticleBookmark;
import io.swkoreatech.kosp.domain.community.article.model.ArticleLike;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleLikeRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.upload.model.Attachment;
import io.swkoreatech.kosp.domain.upload.repository.AttachmentRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public Long create(User author, Board board, ArticleRequest request) {
        if (board.isNotice()) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        
        Article article = Article.builder()
            .author(author)
            .board(board)
            .title(request.title())
            .content(request.content())
            .tags(request.tags())
            .build();
        
        Article savedArticle = articleRepository.save(article);
        
        // Link attachments if provided
        if (request.attachmentIds() != null && !request.attachmentIds().isEmpty()) {
            List<Attachment> attachments =
                attachmentRepository.findAllById(request.attachmentIds());
            
            // Verify uploader and link to article
            attachments.forEach(attachment -> {
                if (!attachment.getUploadedBy().equals(author)) {
                    throw new GlobalException(ExceptionMessage.FORBIDDEN);
                }
                attachment.setArticle(savedArticle);
            });
        }
        
        return savedArticle.getId();
    }

    @Transactional
    public ArticleResponse getOne(Long id, User user) {
        Article article = articleRepository.getById(id);
        
        // Check if article is deleted - regular users cannot see deleted articles
        if (article.isDeleted()) {
            throw new GlobalException(ExceptionMessage.NOT_FOUND);
        }
        
        article.increaseViews();

        boolean isLiked = isLiked(user, article);
        boolean isBookmarked = isBookmarked(user, article);

        return ArticleResponse.from(article, isLiked, isBookmarked);
    }

    public ArticleListResponse<ArticleResponse> getList(Board board, Pageable pageable, User user) {
        Page<Article> page = articleRepository.findByBoardAndIsDeletedFalse(board, pageable);
        return toResponse(page, user);
    }

    public ArticleListResponse<ArticleResponse> getPinnedList(Board board, Pageable pageable, User user) {
        Page<Article> page = articleRepository.findByBoardAndIsPinnedTrueAndIsDeletedFalse(board, pageable);
        return toResponse(page, user);
    }
    
    private ArticleListResponse<ArticleResponse> toResponse(Page<Article> page, User user) {
        List<ArticleResponse> posts = page.getContent().stream()
            .map(article -> ArticleResponse.from(article, isLiked(user, article), isBookmarked(user, article)))
            .toList();
        return new ArticleListResponse<>(posts, PageMeta.from(page));
    }
    
    // Admin methods - include deleted articles
    public ArticleListResponse<AdminArticleResponse> getListForAdmin(Board board, Pageable pageable, User user) {
        Page<Article> page = articleRepository.findByBoard(board, pageable);
        return toAdminResponse(page, user);
    }
    
    @Transactional
    public AdminArticleResponse getOneForAdmin(Long id, User user) {
        Article article = articleRepository.getById(id);
        article.increaseViews();
        
        boolean isLiked = isLiked(user, article);
        boolean isBookmarked = isBookmarked(user, article);
        
        return AdminArticleResponse.from(article, isLiked, isBookmarked);
    }
    
    private ArticleListResponse<AdminArticleResponse> toAdminResponse(Page<Article> page, User user) {
        List<AdminArticleResponse> posts = page.getContent().stream()
            .map(article -> AdminArticleResponse.from(article, isLiked(user, article), isBookmarked(user, article)))
            .toList();
        return new ArticleListResponse<>(posts, PageMeta.from(page));
    }

    @Transactional
    public boolean toggleLike(User user, Long id) {
        Article article = articleRepository.getById(id);
        Optional<ArticleLike> like = articleLikeRepository.findByUserAndArticle(user, article);
        if (like.isPresent()) {
            articleLikeRepository.delete(like.get());
            article.decrementLikes();
            return false;
        }
        articleLikeRepository.save(ArticleLike.builder().user(user).article(article).build());
        article.incrementLikes();
        return true;
    }

    @Transactional
    public boolean toggleBookmark(User user, Long id) {
        Article article = articleRepository.getById(id);
        Optional<ArticleBookmark> bookmark = articleBookmarkRepository.findByUserAndArticle(user, article);
        if (bookmark.isPresent()) {
            articleBookmarkRepository.delete(bookmark.get());
            return false;
        }
        articleBookmarkRepository.save(ArticleBookmark.builder().user(user).article(article).build());
        return true;
    }

    @Transactional
    public void update(User author, Long id, ArticleRequest request) {
        Article article = articleRepository.getById(id);
        validateOwner(article, author.getId());
        article.updateArticle(request.title(), request.content(), request.tags());
    }

    @Transactional
    public void delete(User author, Long id) {
        Article article = articleRepository.getById(id);
        validateOwner(article, author.getId());
        article.delete();
    }

    private void validateOwner(Article article, Long authorId) {
        if (!article.getAuthor().getId().equals(authorId)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

    private boolean isLiked(User user, Article article) {
        return user != null && articleLikeRepository.existsByUserAndArticle(user, article);
    }

    private boolean isBookmarked(User user, Article article) {
        return user != null && articleBookmarkRepository.existsByUserAndArticle(user, article);
    }
}
