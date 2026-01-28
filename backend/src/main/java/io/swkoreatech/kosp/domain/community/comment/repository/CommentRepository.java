package io.swkoreatech.kosp.domain.community.comment.repository;

import java.util.Optional;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface CommentRepository extends Repository<Comment, Long> {

    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    Optional<Comment> findByIdAndIsDeletedFalse(Long id);

    default Comment getById(Long id) {
        return findByIdAndIsDeletedFalse(id).orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
    
    Page<Comment> findByArticleIdAndIsDeletedFalse(Long articleId, Pageable pageable);
    
    Page<Comment> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);
}
