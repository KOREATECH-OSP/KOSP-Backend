package io.swkoreatech.kosp.domain.community.article.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.model.ArticleBookmark;

/**
 * 게시글 북마크 리포지토리.
 * 게시글 북마크의 저장, 삭제 및 조회 기능을 제공한다.
 */
public interface ArticleBookmarkRepository extends Repository<ArticleBookmark, Long> {

    /**
     * 게시글 북마크를 저장한다.
     *
     * @param articleBookmark 저장할 북마크
     * @return 저장된 북마크
     */
    ArticleBookmark save(ArticleBookmark articleBookmark);

    /**
     * 게시글 북마크를 삭제한다.
     *
     * @param articleBookmark 삭제할 북마크
     */
    void delete(ArticleBookmark articleBookmark);

    /**
     * 사용자와 게시글로 북마크를 조회한다.
     *
     * @param user 사용자
     * @param article 게시글
     * @return 북마크 (존재하지 않으면 빈 Optional)
     */
    Optional<ArticleBookmark> findByUserAndArticle(User user, Article article);

    /**
     * 사용자의 게시글 북마크 존재 여부를 확인한다.
     *
     * @param user 사용자
     * @param article 게시글
     * @return 북마크 존재 여부
     */
    boolean existsByUserAndArticle(User user, Article article);

    /**
     * 사용자가 북마크한 게시글 목록을 페이징 조회한다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 북마크한 게시글 페이지
     */
    @Query("SELECT b.article FROM ArticleBookmark b WHERE b.user.id = :userId")
    Page<Article> findArticlesByUserId(@Param("userId") Long userId, Pageable pageable);
}
