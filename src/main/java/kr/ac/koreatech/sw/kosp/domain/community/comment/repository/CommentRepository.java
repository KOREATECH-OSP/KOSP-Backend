package kr.ac.koreatech.sw.kosp.domain.community.comment.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.Comment;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface CommentRepository extends Repository<Comment, Long> {

    Comment save(Comment comment);
    void delete(Comment comment);
    Optional<Comment> findById(Long id);

    default Comment getById(Long id) {
        return findById(id).orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
    
    Page<Comment> findByArticleId(Long articleId, Pageable pageable);
    
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);
}
