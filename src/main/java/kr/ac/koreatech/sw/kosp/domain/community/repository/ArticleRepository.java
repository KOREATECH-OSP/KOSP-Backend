package kr.ac.koreatech.sw.kosp.domain.community.repository;

import kr.ac.koreatech.sw.kosp.domain.community.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a WHERE a.body.content.meta.category = :category")
    Page<Article> findAllByCategory(@Param("category") String category, Pageable pageable);
}
