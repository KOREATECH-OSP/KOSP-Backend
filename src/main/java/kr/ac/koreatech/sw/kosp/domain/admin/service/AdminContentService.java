package kr.ac.koreatech.sw.kosp.domain.admin.service;

import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

    private final ArticleRepository articleRepository;

    @Transactional
    public void deleteArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));
        
        article.delete();
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        // Notice Logic: Currently treated as Article
        deleteArticle(noticeId);
    }
}
