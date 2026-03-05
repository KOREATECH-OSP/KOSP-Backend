package io.swkoreatech.kosp.domain.community.article.repository;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.model.ArticleLike;

import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * 게시글 좋아요 리포지토리.
 * 게시글 좋아요의 저장, 삭제 및 조회 기능을 제공한다.
 */
public interface ArticleLikeRepository extends Repository<ArticleLike, Long> {

    /**
     * 게시글 좋아요를 저장한다.
     *
     * @param articleLike 저장할 좋아요
     * @return 저장된 좋아요
     */
    ArticleLike save(ArticleLike articleLike);

    /**
     * 게시글 좋아요를 삭제한다.
     *
     * @param articleLike 삭제할 좋아요
     */
    void delete(ArticleLike articleLike);

    /**
     * 사용자와 게시글로 좋아요를 조회한다.
     *
     * @param user 사용자
     * @param article 게시글
     * @return 좋아요 (존재하지 않으면 빈 Optional)
     */
    Optional<ArticleLike> findByUserAndArticle(User user, Article article);

    /**
     * 사용자의 게시글 좋아요 존재 여부를 확인한다.
     *
     * @param user 사용자
     * @param article 게시글
     * @return 좋아요 존재 여부
     */
    boolean existsByUserAndArticle(User user, Article article);
}
