package kr.ac.koreatech.sw.kosp.domain.community.article.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.ArticleLike;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import org.springframework.data.repository.Repository;

public interface ArticleLikeRepository extends Repository<ArticleLike, Long> {
    ArticleLike save(ArticleLike articleLike);
    void delete(ArticleLike articleLike);
    Optional<ArticleLike> findByUserAndArticle(User user, Article article);
    boolean existsByUserAndArticle(User user, Article article);
}
