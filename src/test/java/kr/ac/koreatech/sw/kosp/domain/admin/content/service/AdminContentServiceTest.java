package kr.ac.koreatech.sw.kosp.domain.admin.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.ac.koreatech.sw.kosp.domain.admin.content.dto.request.NoticeCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.content.dto.request.NoticeUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.Comment;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminContentService 단위 테스트")
class AdminContentServiceTest {

    @InjectMocks
    private AdminContentService adminContentService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CommentRepository commentRepository;

    private Article createArticle(Long id, String title) {
        Article article = Article.builder()
            .title(title)
            .content("테스트 내용")
            .build();
        ReflectionTestUtils.setField(article, "id", id);
        ReflectionTestUtils.setField(article, "isDeleted", false);
        return article;
    }

    private Board createBoard(Long id, String name) {
        Board board = Board.builder()
            .name(name)
            .description(name + " 게시판")
            .build();
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }

    private User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutEmail(name + "@koreatech.ac.kr")
            .kutId("2024" + id)
            .password("password")
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Comment createComment(Long id) {
        Comment comment = Comment.builder()
            .content("테스트 댓글")
            .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    @Nested
    @DisplayName("deleteArticle 메서드")
    class DeleteArticleTest {

        @Test
        @DisplayName("존재하지 않는 게시글을 삭제하면 예외가 발생한다")
        void throwsException_whenArticleNotFound() {
            // given
            given(articleRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminContentService.deleteArticle(999L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("게시글을 성공적으로 소프트 삭제한다")
        void softDeletesArticleSuccessfully() {
            // given
            Article article = createArticle(1L, "테스트 게시글");
            given(articleRepository.findById(1L)).willReturn(Optional.of(article));

            // when
            adminContentService.deleteArticle(1L);

            // then
            assertThat(article.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteNotice 메서드")
    class DeleteNoticeTest {

        @Test
        @DisplayName("공지사항을 성공적으로 삭제한다")
        void deletesNoticeSuccessfully() {
            // given
            Article notice = createArticle(1L, "공지사항");
            given(articleRepository.findById(1L)).willReturn(Optional.of(notice));

            // when
            adminContentService.deleteNotice(1L);

            // then
            assertThat(notice.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteComment 메서드")
    class DeleteCommentTest {

        @Test
        @DisplayName("존재하지 않는 댓글을 삭제하면 예외가 발생한다")
        void throwsException_whenCommentNotFound() {
            // given
            given(commentRepository.getById(999L)).willThrow(new GlobalException(ExceptionMessage.NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> adminContentService.deleteComment(999L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("댓글을 성공적으로 삭제한다")
        void deletesCommentSuccessfully() {
            // given
            Comment comment = createComment(1L);
            given(commentRepository.getById(1L)).willReturn(comment);

            // when
            adminContentService.deleteComment(1L);

            // then
            verify(commentRepository).delete(comment);
        }
    }

    @Nested
    @DisplayName("createNotice 메서드")
    class CreateNoticeTest {

        @Test
        @DisplayName("공지사항 게시판이 없으면 예외가 발생한다")
        void throwsException_whenNoticeBoardNotFound() {
            // given
            User user = createUser(1L, "관리자");
            NoticeCreateRequest request = new NoticeCreateRequest("공지 제목", "공지 내용", true, List.of("태그"));
            given(boardRepository.findAll()).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> adminContentService.createNotice(user, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("공지사항 게시판(한글)에 공지를 생성한다")
        void createsNotice_whenKoreanBoardExists() {
            // given
            User user = createUser(1L, "관리자");
            Board noticeBoard = createBoard(1L, "공지사항");
            NoticeCreateRequest request = new NoticeCreateRequest("공지 제목", "공지 내용", true, List.of("태그"));
            
            given(boardRepository.findAll()).willReturn(List.of(noticeBoard));

            // when
            adminContentService.createNotice(user, request);

            // then
            verify(articleRepository).save(any(Article.class));
        }

        @Test
        @DisplayName("NOTICE 게시판(영문)에 공지를 생성한다")
        void createsNotice_whenEnglishBoardExists() {
            // given
            User user = createUser(1L, "관리자");
            Board noticeBoard = createBoard(1L, "NOTICE");
            NoticeCreateRequest request = new NoticeCreateRequest("공지 제목", "공지 내용", false, List.of());
            
            given(boardRepository.findAll()).willReturn(List.of(noticeBoard));

            // when
            adminContentService.createNotice(user, request);

            // then
            verify(articleRepository).save(any(Article.class));
        }

        @Test
        @DisplayName("여러 게시판 중 공지사항 게시판을 찾아서 공지를 생성한다")
        void findsNoticeBoardAmongMultipleBoards() {
            // given
            User user = createUser(1L, "관리자");
            Board generalBoard = createBoard(1L, "자유게시판");
            Board noticeBoard = createBoard(2L, "공지사항");
            Board qnaBoard = createBoard(3L, "Q&A");
            NoticeCreateRequest request = new NoticeCreateRequest("공지 제목", "공지 내용", true, List.of());
            
            given(boardRepository.findAll()).willReturn(List.of(generalBoard, noticeBoard, qnaBoard));

            // when
            adminContentService.createNotice(user, request);

            // then
            verify(articleRepository).save(any(Article.class));
        }
    }

    @Nested
    @DisplayName("updateNotice 메서드")
    class UpdateNoticeTest {

        @Test
        @DisplayName("존재하지 않는 공지를 수정하면 예외가 발생한다")
        void throwsException_whenNoticeNotFound() {
            // given
            given(articleRepository.findById(999L)).willReturn(Optional.empty());
            NoticeUpdateRequest request = new NoticeUpdateRequest("수정 제목", "수정 내용", true, List.of());

            // when & then
            assertThatThrownBy(() -> adminContentService.updateNotice(999L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("공지사항을 성공적으로 수정한다")
        void updatesNoticeSuccessfully() {
            // given
            Article notice = createArticle(1L, "기존 제목");
            given(articleRepository.findById(1L)).willReturn(Optional.of(notice));
            NoticeUpdateRequest request = new NoticeUpdateRequest("수정 제목", "수정 내용", true, List.of("새태그"));

            // when
            adminContentService.updateNotice(1L, request);

            // then
            assertThat(notice.getTitle()).isEqualTo("수정 제목");
            assertThat(notice.getContent()).isEqualTo("수정 내용");
            assertThat(notice.isPinned()).isTrue();
            assertThat(notice.getTags()).containsExactly("새태그");
        }

        @Test
        @DisplayName("고정 해제 상태로 수정할 수 있다")
        void canUnpinNotice() {
            // given
            Article notice = createArticle(1L, "기존 제목");
            ReflectionTestUtils.setField(notice, "isPinned", true);
            given(articleRepository.findById(1L)).willReturn(Optional.of(notice));
            NoticeUpdateRequest request = new NoticeUpdateRequest("수정 제목", "수정 내용", false, List.of());

            // when
            adminContentService.updateNotice(1L, request);

            // then
            assertThat(notice.isPinned()).isFalse();
        }
    }
}
