package io.swkoreatech.kosp.domain.community.article.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;

public interface ArticleRepository extends Repository<Article, Long>, JpaSpecificationExecutor<Article> {

    Article save(Article article);

    Optional<Article> findById(Long id);

    void delete(Article article);

    Page<Article> findByBoard(Board board, Pageable pageable);
    
    Page<Article> findByBoardAndIsDeletedFalse(Board board, Pageable pageable);
    
    Page<Article> findByBoardAndIsPinnedTrueAndIsDeletedFalse(Board board, Pageable pageable);
    
    java.util.List<Article> findByTitleContaining(String keyword);
    java.util.List<Article> findByTitleContainingAndIsDeletedFalse(String title);

    default Article getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
    
    Page<Article> findByAuthor(User author, Pageable pageable);

    Page<Article> findAll(Specification<Article> spec, Pageable pageable);
}
