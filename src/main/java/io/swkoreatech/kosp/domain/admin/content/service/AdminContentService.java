package io.swkoreatech.kosp.domain.admin.content.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeUpdateRequest;
import io.swkoreatech.kosp.domain.community.board.repository.BoardRepository;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentRepository;
import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeCreateRequest;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

    private final ArticleRepository articleRepository;

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void deleteArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));
        
        article.delete();
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        deleteArticle(noticeId);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        var comment = commentRepository.getById(commentId);
        // comment.delete(); // Comment 엔티티에 delete 메서드 필요. 현재는 리포지토리로 삭제 대체
        commentRepository.delete(comment);
    }

    @Transactional
    public void createNotice(User user, NoticeCreateRequest request) {
        // Find "NOTICE" board or "공지사항"
        var board = boardRepository.findAll().stream()
            .filter(b -> "공지사항".equals(b.getName()) || "NOTICE".equalsIgnoreCase(b.getName()))
            .findFirst()
            .orElseThrow(() -> new GlobalException(ExceptionMessage.BOARD_NOT_FOUND));

        Article notice = Article.builder()
            .author(user)
            .board(board)
            .title(request.title())
            .content(request.content())
            .isPinned(request.isPinned())
            .tags(request.tags())
            .build();

        articleRepository.save(notice);
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Article notice = articleRepository.findById(noticeId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));
        
        notice.updateArticle(request.title(), request.content(), request.isPinned(), request.tags());
    }


}
