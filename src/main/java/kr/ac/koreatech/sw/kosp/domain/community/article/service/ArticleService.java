package kr.ac.koreatech.sw.kosp.domain.community.article.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.ArticleBookmark;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.ArticleLike;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final kr.ac.koreatech.sw.kosp.domain.upload.repository.AttachmentRepository attachmentRepository;

    @Transactional
    public Long create(User author, Board board, ArticleRequest req) {
        Article article = Article.builder()
            .author(author)
            .board(board)
            .title(req.title())
            .content(req.content())
            .tags(req.tags())
            .build();
        
        Article savedArticle = articleRepository.save(article);
        
        // Link attachments if provided
        if (req.attachmentIds() != null && !req.attachmentIds().isEmpty()) {
            List<kr.ac.koreatech.sw.kosp.domain.upload.model.Attachment> attachments = 
                attachmentRepository.findAllById(req.attachmentIds());
            
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
        // Regular users - exclude deleted articles
        Page<Article> page = articleRepository.findByBoardAndIsDeletedFalse(board, pageable);
        return toResponse(page, user);
    }
    
    private ArticleListResponse<ArticleResponse> toResponse(Page<Article> page, User user) {
        List<ArticleResponse> posts = page.getContent().stream()
            .map(article -> ArticleResponse.from(article, isLiked(user, article), isBookmarked(user, article)))
            .toList();
        return new ArticleListResponse<>(posts, PageMeta.from(page));
    }
    
    // Admin methods - include deleted articles
    public ArticleListResponse<kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.AdminArticleResponse> getListForAdmin(Board board, Pageable pageable, User user) {
        Page<Article> page = articleRepository.findByBoard(board, pageable);
        return toAdminResponse(page, user);
    }
    
    public kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.AdminArticleResponse getOneForAdmin(Long id, User user) {
        Article article = articleRepository.getById(id);
        article.increaseViews();
        
        boolean isLiked = isLiked(user, article);
        boolean isBookmarked = isBookmarked(user, article);
        
        return kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.AdminArticleResponse.from(article, isLiked, isBookmarked);
    }
    
    private ArticleListResponse<kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.AdminArticleResponse> toAdminResponse(Page<Article> page, User user) {
        List<kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.AdminArticleResponse> posts = page.getContent().stream()
            .map(article -> kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.AdminArticleResponse.from(article, isLiked(user, article), isBookmarked(user, article)))
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
    public void update(User author, Long id, ArticleRequest req) {
        Article article = articleRepository.getById(id);
        validateOwner(article, author.getId());
        article.updateArticle(req.title(), req.content(), req.tags());
    }

    @Transactional
    public void delete(User author, Long id) {
        Article article = articleRepository.getById(id);
        validateOwner(article, author.getId());
        articleRepository.delete(article);
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
