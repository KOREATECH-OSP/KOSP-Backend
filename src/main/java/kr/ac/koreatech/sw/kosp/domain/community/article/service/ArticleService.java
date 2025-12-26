package kr.ac.koreatech.sw.kosp.domain.community.article.service;

import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long authorId, ArticleRequest req) {
        Board board = boardRepository.getById(req.boardId());
        User author = userRepository.getById(authorId);
        
        Article article = Article.create(author, board, req.title(), req.content(), req.tags());
        articleRepository.save(article);
        return article.getId();
    }

    public ArticleResponse getOne(Long id) {
        Article article = articleRepository.getById(id);
        article.increaseViews(); 
        return ArticleResponse.from(article);
    }

    public ArticleListResponse getList(Long boardId, Pageable pageable) {
        Board board = boardRepository.getById(boardId);
        Page<Article> page = articleRepository.findByBoard(board, pageable);
        return ArticleListResponse.from(page);
    }

    @Transactional
    public void update(Long authorId, Long id, ArticleRequest req) {
        Article article = articleRepository.getById(id);
        validateOwner(article, authorId);
        article.update(req.title(), req.content(), req.tags());
    }

    @Transactional
    public void delete(Long authorId, Long id) {
        Article article = articleRepository.getById(id);
        validateOwner(article, authorId);
        articleRepository.delete(article);
    }

    private void validateOwner(Article article, Long authorId) {
        if (!article.getAuthor().getId().equals(authorId)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }
}
