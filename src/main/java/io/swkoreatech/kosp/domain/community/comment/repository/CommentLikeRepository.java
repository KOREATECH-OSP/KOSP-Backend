package io.swkoreatech.kosp.domain.community.comment.repository;

import java.util.Optional;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.community.comment.model.CommentLike;
import io.swkoreatech.kosp.domain.user.model.User;
import org.springframework.data.repository.Repository;

public interface CommentLikeRepository extends Repository<CommentLike, Long> {

    CommentLike save(CommentLike commentLike);
    void delete(CommentLike commentLike);
    boolean existsByUserAndComment(User user, Comment comment);
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
}
