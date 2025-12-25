package kr.ac.koreatech.sw.kosp.domain.community.article.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface ArticleRepository extends Repository<Article, Long> {

    Article save(Article article);

    Optional<Article> findById(Long id);

    void delete(Article article);

    Page<Article> findByBoard(Board board, Pageable pageable);

    default Article getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));
    }
}
