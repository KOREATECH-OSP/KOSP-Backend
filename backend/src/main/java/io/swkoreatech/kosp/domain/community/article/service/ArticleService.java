package io.swkoreatech.kosp.domain.community.article.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.dto.request.ArticleRequest;
import io.swkoreatech.kosp.domain.community.article.dto.response.AdminArticleResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ToggleBookmarkResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ToggleLikeResponse;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.model.ArticleBookmark;
import io.swkoreatech.kosp.domain.community.article.model.ArticleLike;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleLikeRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.upload.model.Attachment;
import io.swkoreatech.kosp.domain.upload.repository.AttachmentRepository;
import io.swkoreatech.kosp.global.dto.PageMeta;
import lombok.RequiredArgsConstructor;

/**
 * 게시글 서비스.
 * 게시글의 CRUD, 좋아요, 북마크 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final AttachmentRepository attachmentRepository;

    /**
     * 게시글을 작성한다.
     *
     * @param author 작성자
     * @param board 게시판
     * @param request 게시글 작성 요청
     * @return 생성된 게시글 ID
     * @throws GlobalException 공지사항 게시판에 작성 시도 시 또는 첨부파일 소유자가 아닌 경우
     */
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

    /**
     * 게시글 상세 정보를 조회한다. 조회 시 조회수가 증가한다.
     *
     * @param id 게시글 ID
     * @param user 조회하는 사용자
     * @return 게시글 응답
     * @throws GlobalException 삭제된 게시글인 경우
     */
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

    /**
     * 게시판의 게시글 목록을 조회한다.
     *
     * @param board 게시판
     * @param pageable 페이징 정보
     * @param user 조회하는 사용자
     * @return 게시글 목록 응답
     */
    public ArticleListResponse<ArticleResponse> getList(Board board, Pageable pageable, User user) {
        Page<Article> page = articleRepository.findByBoardAndIsDeletedFalse(board, pageable);
        return toResponse(page, user);
    }

    /**
     * 게시판의 고정 게시글 목록을 조회한다.
     *
     * @param board 게시판
     * @param pageable 페이징 정보
     * @param user 조회하는 사용자
     * @return 고정 게시글 목록 응답
     */
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

    /**
     * 관리자용 게시글 목록을 조회한다. 삭제된 게시글도 포함된다.
     *
     * @param board 게시판
     * @param pageable 페이징 정보
     * @param user 관리자 사용자
     * @return 관리자용 게시글 목록 응답
     */
    public ArticleListResponse<AdminArticleResponse> getListForAdmin(Board board, Pageable pageable, User user) {
        Page<Article> page = articleRepository.findByBoard(board, pageable);
        return toAdminResponse(page, user);
    }

    /**
     * 관리자용 게시글 상세를 조회한다. 삭제된 게시글도 조회 가능하다.
     *
     * @param id 게시글 ID
     * @param user 관리자 사용자
     * @return 관리자용 게시글 응답
     */
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

    /**
     * 게시글 좋아요를 토글한다.
     *
     * @param user 사용자
     * @param id 게시글 ID
     * @return 좋아요 토글 응답
     */
    @Transactional
    public ToggleLikeResponse toggleLike(User user, Long id) {
        Article article = articleRepository.getById(id);
        Optional<ArticleLike> like = articleLikeRepository.findByUserAndArticle(user, article);
        if (like.isPresent()) {
            articleLikeRepository.delete(like.get());
            article.decrementLikes();
            return ToggleLikeResponse.from(false);
        }
        articleLikeRepository.save(ArticleLike.builder().user(user).article(article).build());
        article.incrementLikes();
        return ToggleLikeResponse.from(true);
    }

    /**
     * 게시글 북마크를 토글한다.
     *
     * @param user 사용자
     * @param id 게시글 ID
     * @return 북마크 토글 응답
     */
    @Transactional
    public ToggleBookmarkResponse toggleBookmark(User user, Long id) {
        Article article = articleRepository.getById(id);
        Optional<ArticleBookmark> bookmark = articleBookmarkRepository.findByUserAndArticle(user, article);
        if (bookmark.isPresent()) {
            articleBookmarkRepository.delete(bookmark.get());
            return ToggleBookmarkResponse.from(false);
        }
        articleBookmarkRepository.save(ArticleBookmark.builder().user(user).article(article).build());
        return ToggleBookmarkResponse.from(true);
    }

    /**
     * 게시글을 수정한다.
     *
     * @param author 수정 요청 사용자
     * @param id 게시글 ID
     * @param request 수정 요청
     * @throws GlobalException 작성자가 아닌 경우
     */
    @Transactional
    public void update(User author, Long id, ArticleRequest request) {
        Article article = articleRepository.getById(id);
        validateOwner(article, author.getId());
        article.updateArticle(request.title(), request.content(), request.tags());
    }

    /**
     * 게시글을 삭제한다.
     *
     * @param author 삭제 요청 사용자
     * @param id 게시글 ID
     * @throws GlobalException 작성자가 아닌 경우
     */
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
