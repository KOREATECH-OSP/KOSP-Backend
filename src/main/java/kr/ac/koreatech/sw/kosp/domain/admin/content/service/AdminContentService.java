package kr.ac.koreatech.sw.kosp.domain.admin.content.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.admin.content.dto.request.NoticeCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

    private final ArticleRepository articleRepository;

    private final kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository boardRepository;
    private final kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentRepository commentRepository;

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
    public void updateNotice(Long noticeId, kr.ac.koreatech.sw.kosp.domain.admin.content.dto.request.NoticeUpdateRequest request) {
        Article notice = articleRepository.findById(noticeId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));
        
        notice.updateArticle(request.title(), request.content(), request.isPinned(), request.tags());
    }

    @Transactional
    public boolean toggleArticlePinned(Long articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));
        return article.togglePinned();
    }


}
