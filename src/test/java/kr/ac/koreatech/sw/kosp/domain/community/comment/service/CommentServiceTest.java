package kr.ac.koreatech.sw.kosp.domain.community.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.Comment;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.CommentLike;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private ArticleRepository articleRepository;

    private User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Board createBoard(Long id) {
        Board board = Board.builder()
            .name("자유게시판")
            .description("설명")
            .build();
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }

    private Article createArticle(Long id, User author) {
        Board board = createBoard(1L);
        Article article = Article.builder()
            .author(author)
            .board(board)
            .title("글 제목")
            .content("글 내용")
            .build();
        ReflectionTestUtils.setField(article, "id", id);
        ReflectionTestUtils.setField(article, "commentsCount", 0);
        return article;
    }

    private Comment createComment(Long id, User author, Article article) {
        Comment comment = Comment.builder()
            .author(author)
            .article(article)
            .content("댓글 내용")
            .build();
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(comment, "createdAt", java.time.LocalDateTime.now());
        return comment;
    }

    @Nested
    @DisplayName("create 메서드")
    class CreateTest {

        @Test
        @DisplayName("댓글을 성공적으로 작성하고 게시글 댓글 수가 증가한다")
        void createsComment_andIncrementsCount() {
            // given
            User author = createUser(1L, "작성자");
            Article article = createArticle(1L, author);
            CommentCreateRequest request = new CommentCreateRequest("새 댓글");
            
            given(articleRepository.getById(1L)).willReturn(article);
            doAnswer(invocation -> {
                Comment comment = invocation.getArgument(0);
                ReflectionTestUtils.setField(comment, "id", 1L);
                return comment;
            }).when(commentRepository).save(any(Comment.class));

            // when
            Long commentId = commentService.create(author, 1L, request);

            // then
            assertThat(commentId).isEqualTo(1L);
            assertThat(article.getCommentsCount()).isEqualTo(1);
            verify(commentRepository).save(any(Comment.class));
            verify(articleRepository).save(article);
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteTest {

        @Test
        @DisplayName("작성자가 아니면 삭제 시 예외가 발생한다")
        void throwsException_whenNotOwner() {
            // given
            User author = createUser(1L, "작성자");
            User other = createUser(2L, "다른 사용자");
            Article article = createArticle(1L, author);
            Comment comment = createComment(1L, author, article);
            
            given(commentRepository.getById(1L)).willReturn(comment);

            // when & then
            assertThatThrownBy(() -> commentService.delete(other, 1L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("작성자가 댓글을 성공적으로 삭제하고 게시글 댓글 수가 감소한다")
        void deletesComment_andDecrementsCount() {
            // given
            User author = createUser(1L, "작성자");
            Article article = createArticle(1L, author);
            ReflectionTestUtils.setField(article, "commentsCount", 1);
            Comment comment = createComment(1L, author, article);
            
            given(commentRepository.getById(1L)).willReturn(comment);

            // when
            commentService.delete(author, 1L);

            // then
            assertThat(article.getCommentsCount()).isEqualTo(0);
            verify(commentRepository).delete(comment);
            verify(articleRepository).save(article);
        }
    }

    @Nested
    @DisplayName("getList 메서드")
    class GetListTest {

        @Test
        @DisplayName("게시글의 댓글 목록을 페이징하여 조회한다")
        void returnsPagedCommentList() {
            // given
            User author = createUser(1L, "작성자");
            Article article = createArticle(1L, author);
            Comment comment = createComment(1L, author, article);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> page = new PageImpl<>(List.of(comment), pageable, 1);
            
            given(commentRepository.findByArticleId(1L, pageable)).willReturn(page);

            // when
            CommentListResponse result = commentService.getList(1L, pageable, author);

            // then
            assertThat(result.comments()).hasSize(1);
        }

        @Test
        @DisplayName("비로그인 사용자도 댓글 목록을 조회할 수 있다")
        void returnsCommentList_forAnonymousUser() {
            // given
            User author = createUser(1L, "작성자");
            Article article = createArticle(1L, author);
            Comment comment = createComment(1L, author, article);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> page = new PageImpl<>(List.of(comment), pageable, 1);
            
            given(commentRepository.findByArticleId(1L, pageable)).willReturn(page);

            // when
            CommentListResponse result = commentService.getList(1L, pageable, null);

            // then
            assertThat(result.comments()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("toggleLike 메서드")
    class ToggleLikeTest {

        @Test
        @DisplayName("좋아요가 없으면 추가한다")
        void addsLike_whenNotExists() {
            // given
            User user = createUser(1L, "사용자");
            Article article = createArticle(1L, user);
            Comment comment = createComment(1L, user, article);
            
            given(commentRepository.getById(1L)).willReturn(comment);
            given(commentLikeRepository.findByUserAndComment(user, comment)).willReturn(Optional.empty());

            // when
            boolean result = commentService.toggleLike(user, 1L);

            // then
            assertThat(result).isTrue();
            verify(commentLikeRepository).save(any(CommentLike.class));
        }

        @Test
        @DisplayName("좋아요가 있으면 제거한다")
        void removesLike_whenExists() {
            // given
            User user = createUser(1L, "사용자");
            Article article = createArticle(1L, user);
            Comment comment = createComment(1L, user, article);
            CommentLike like = CommentLike.builder().user(user).comment(comment).build();
            
            given(commentRepository.getById(1L)).willReturn(comment);
            given(commentLikeRepository.findByUserAndComment(user, comment)).willReturn(Optional.of(like));

            // when
            boolean result = commentService.toggleLike(user, 1L);

            // then
            assertThat(result).isFalse();
            verify(commentLikeRepository).delete(like);
        }
    }
}
