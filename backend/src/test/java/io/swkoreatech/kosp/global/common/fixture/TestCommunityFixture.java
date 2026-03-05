package io.swkoreatech.kosp.global.common.fixture;

import java.time.LocalDateTime;

import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;

public final class TestCommunityFixture {

    private TestCommunityFixture() {
    }

    public static Board createBoard(Long id, String name) {
        Board board = Board.builder()
            .name(name)
            .description(name + " 게시판")
            .build();
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }

    public static Board createBoard(
        Long id, String name, boolean isNotice
    ) {
        Board board = createBoard(id, name);
        ReflectionTestUtils.setField(board, "isNotice", isNotice);
        return board;
    }

    public static Article createArticle(
        Long id, User author, Board board, String title
    ) {
        Article article = Article.builder()
            .author(author)
            .board(board)
            .title(title)
            .content("내용")
            .build();
        ReflectionTestUtils.setField(article, "id", id);
        ReflectionTestUtils.setField(article, "isDeleted", false);
        ReflectionTestUtils.setField(
            article, "createdAt", LocalDateTime.now()
        );
        return article;
    }

    public static Article createArticle(Long id, String title) {
        Article article = Article.builder()
            .title(title)
            .content("테스트 내용")
            .build();
        ReflectionTestUtils.setField(article, "id", id);
        ReflectionTestUtils.setField(article, "isDeleted", false);
        return article;
    }

    public static Comment createComment(
        Long id, User author, Article article
    ) {
        Comment comment = Comment.builder()
            .author(author)
            .article(article)
            .content("댓글 내용")
            .build();
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(
            comment, "createdAt", LocalDateTime.now()
        );
        return comment;
    }

    public static Comment createComment(Long id) {
        Comment comment = Comment.builder()
            .content("테스트 댓글")
            .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}
