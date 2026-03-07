package io.swkoreatech.kosp.domain.admin.content.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeCreateRequest;
import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeUpdateRequest;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.board.repository.BoardRepository;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 콘텐츠 관리 서비스.
 * <p>게시글, 공지사항, 댓글의 삭제 및 공지사항 생성/수정 비즈니스 로직을 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

    private final ArticleRepository articleRepository;

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    /**
     * 게시글을 소프트 삭제한다.
     *
     * @param articleId 삭제할 게시글 식별자
     * @throws GlobalException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));

        article.delete();
    }

    /**
     * 공지사항을 삭제한다.
     *
     * @param noticeId 삭제할 공지사항 식별자
     * @throws GlobalException 공지사항을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteNotice(Long noticeId) {
        deleteArticle(noticeId);
    }

    /**
     * 댓글을 소프트 삭제한다.
     *
     * @param commentId 삭제할 댓글 식별자
     */
    @Transactional
    public void deleteComment(Long commentId) {
        var comment = commentRepository.getById(commentId);
        comment.delete();
    }

    /**
     * 공지사항을 생성한다.
     *
     * @param user    작성자 (관리자)
     * @param request 공지사항 생성 요청 DTO
     * @throws GlobalException 공지사항 게시판을 찾을 수 없는 경우
     */
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

    /**
     * 공지사항을 수정한다.
     *
     * @param noticeId 수정할 공지사항 식별자
     * @param request  공지사항 수정 요청 DTO
     * @throws GlobalException 공지사항을 찾을 수 없는 경우
     */
    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Article notice = articleRepository.findById(noticeId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));

        notice.updateArticle(request.title(), request.content(), request.isPinned(), request.tags());
    }

}
