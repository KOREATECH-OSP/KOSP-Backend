package kr.ac.koreatech.sw.kosp.domain.community.article.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.ArticleBookmark;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ArticleBookmarkRepository extends Repository<ArticleBookmark, Long> {
    ArticleBookmark save(ArticleBookmark articleBookmark);
    void delete(ArticleBookmark articleBookmark);
    Optional<ArticleBookmark> findByUserAndArticle(User user, Article article);
    boolean existsByUserAndArticle(User user, Article article);

    @Query("SELECT b.article FROM ArticleBookmark b WHERE b.user.id = :userId")
    Page<Article> findArticlesByUserId(@Param("userId") Long userId, Pageable pageable);
}
