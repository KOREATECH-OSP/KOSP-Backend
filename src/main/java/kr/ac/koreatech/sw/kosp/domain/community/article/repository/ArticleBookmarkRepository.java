package kr.ac.koreatech.sw.kosp.domain.community.article.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.ArticleBookmark;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import org.springframework.data.repository.Repository;

public interface ArticleBookmarkRepository extends Repository<ArticleBookmark, Long> {
    ArticleBookmark save(ArticleBookmark articleBookmark);
    void delete(ArticleBookmark articleBookmark);
    Optional<ArticleBookmark> findByUserAndArticle(User user, Article article);
    boolean existsByUserAndArticle(User user, Article article);
}
