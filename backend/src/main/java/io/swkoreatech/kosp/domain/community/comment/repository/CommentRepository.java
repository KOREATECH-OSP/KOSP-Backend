package io.swkoreatech.kosp.domain.community.comment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;

/**
 * 댓글 리포지토리.
 * 댓글의 저장, 삭제 및 다양한 조건 조회 기능을 제공한다.
 */
public interface CommentRepository extends Repository<Comment, Long> {

    /**
     * 댓글을 저장한다.
     *
     * @param comment 저장할 댓글
     * @return 저장된 댓글
     */
    Comment save(Comment comment);

    /**
     * ID로 댓글을 조회한다.
     *
     * @param id 댓글 ID
     * @return 댓글 (존재하지 않으면 빈 Optional)
     */
    Optional<Comment> findById(Long id);

    /**
     * ID로 삭제되지 않은 댓글을 조회한다.
     *
     * @param id 댓글 ID
     * @return 댓글 (존재하지 않으면 빈 Optional)
     */
    Optional<Comment> findByIdAndIsDeletedFalse(Long id);

    /**
     * ID로 삭제되지 않은 댓글을 조회하고, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param id 댓글 ID
     * @return 댓글
     * @throws GlobalException 댓글이 존재하지 않는 경우
     */
    default Comment getById(Long id) {
        return findByIdAndIsDeletedFalse(id).orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    /**
     * 게시글 ID로 삭제되지 않은 댓글을 페이징 조회한다.
     *
     * @param articleId 게시글 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    Page<Comment> findByArticleIdAndIsDeletedFalse(Long articleId, Pageable pageable);

    /**
     * 작성자 ID로 삭제되지 않은 댓글을 페이징 조회한다.
     *
     * @param authorId 작성자 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    Page<Comment> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);

    /**
     * 댓글을 삭제한다.
     *
     * @param comment 삭제할 댓글
     */
    void delete(Comment comment);

    /**
     * 게시글 ID로 삭제되지 않은 댓글을 페이징 조회한다.
     *
     * @param articleId 게시글 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    default Page<Comment> findByArticleId(Long articleId, Pageable pageable) {
        return findByArticleIdAndIsDeletedFalse(articleId, pageable);
    }
}
