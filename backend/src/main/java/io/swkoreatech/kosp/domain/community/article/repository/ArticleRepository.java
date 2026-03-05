package io.swkoreatech.kosp.domain.community.article.repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.board.model.Board;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

/**
 * 게시글 리포지토리.
 * 게시글의 CRUD 및 다양한 조건 조회 기능을 제공한다.
 */
public interface ArticleRepository extends Repository<Article, Long>, JpaSpecificationExecutor<Article> {

    /**
     * 게시글을 저장한다.
     *
     * @param article 저장할 게시글
     * @return 저장된 게시글
     */
    Article save(Article article);

    /**
     * ID로 게시글을 조회한다.
     *
     * @param id 게시글 ID
     * @return 게시글 (존재하지 않으면 빈 Optional)
     */
    Optional<Article> findById(Long id);

    /**
     * 게시글을 삭제한다.
     *
     * @param article 삭제할 게시글
     */
    void delete(Article article);

    /**
     * 게시판별 게시글을 페이징 조회한다.
     *
     * @param board 게시판
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Article> findByBoard(Board board, Pageable pageable);

    /**
     * 게시판별 삭제되지 않은 게시글을 페이징 조회한다.
     *
     * @param board 게시판
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Article> findByBoardAndIsDeletedFalse(Board board, Pageable pageable);

    /**
     * 게시판별 삭제되지 않은 고정 게시글을 페이징 조회한다.
     *
     * @param board 게시판
     * @param pageable 페이징 정보
     * @return 고정 게시글 페이지
     */
    Page<Article> findByBoardAndIsPinnedTrueAndIsDeletedFalse(Board board, Pageable pageable);

    /**
     * 제목에 키워드를 포함하는 게시글을 조회한다.
     *
     * @param keyword 검색 키워드
     * @return 게시글 목록
     */
    java.util.List<Article> findByTitleContaining(String keyword);

    /**
     * 제목에 키워드를 포함하는 삭제되지 않은 게시글을 조회한다.
     *
     * @param title 검색 키워드
     * @return 게시글 목록
     */
    java.util.List<Article> findByTitleContainingAndIsDeletedFalse(String title);

    /**
     * ID로 게시글을 조회하고, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param id 게시글 ID
     * @return 게시글
     * @throws GlobalException 게시글이 존재하지 않는 경우
     */
    default Article getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    /**
     * 작성자별 게시글을 페이징 조회한다.
     *
     * @param author 작성자
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Article> findByAuthor(User author, Pageable pageable);

    /**
     * 동적 조건으로 게시글을 페이징 조회한다.
     *
     * @param spec 검색 조건
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Article> findAll(Specification<Article> spec, Pageable pageable);
}
