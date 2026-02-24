package io.swkoreatech.kosp.domain.community.comment.repository;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.community.comment.model.CommentLike;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface CommentLikeRepository extends Repository<CommentLike, Long> {

    CommentLike save(CommentLike commentLike);
    void delete(CommentLike commentLike);
    void deleteAllByComment(Comment comment);
    boolean existsByUserAndComment(User user, Comment comment);
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
}
