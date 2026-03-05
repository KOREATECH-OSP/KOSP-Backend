package io.swkoreatech.kosp.domain.community.comment.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.community.comment.model.CommentLike;

/**
 * 댓글 좋아요 리포지토리.
 * 댓글 좋아요의 저장, 삭제 및 조회 기능을 제공한다.
 */
public interface CommentLikeRepository extends Repository<CommentLike, Long> {

    /**
     * 댓글 좋아요를 저장한다.
     *
     * @param commentLike 저장할 좋아요
     * @return 저장된 좋아요
     */
    CommentLike save(CommentLike commentLike);

    /**
     * 댓글 좋아요를 삭제한다.
     *
     * @param commentLike 삭제할 좋아요
     */
    void delete(CommentLike commentLike);

    /**
     * 댓글에 속한 모든 좋아요를 삭제한다.
     *
     * @param comment 대상 댓글
     */
    void deleteAllByComment(Comment comment);

    /**
     * 사용자의 댓글 좋아요 존재 여부를 확인한다.
     *
     * @param user 사용자
     * @param comment 댓글
     * @return 좋아요 존재 여부
     */
    boolean existsByUserAndComment(User user, Comment comment);

    /**
     * 사용자와 댓글로 좋아요를 조회한다.
     *
     * @param user 사용자
     * @param comment 댓글
     * @return 좋아요 (존재하지 않으면 빈 Optional)
     */
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
}
