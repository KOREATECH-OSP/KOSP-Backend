package io.swkoreatech.kosp.domain.community.article.repository;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.model.ArticleLike;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface ArticleLikeRepository extends Repository<ArticleLike, Long> {

    ArticleLike save(ArticleLike articleLike);
    void delete(ArticleLike articleLike);
    Optional<ArticleLike> findByUserAndArticle(User user, Article article);
    boolean existsByUserAndArticle(User user, Article article);
}
