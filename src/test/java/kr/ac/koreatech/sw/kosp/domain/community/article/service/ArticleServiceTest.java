package kr.ac.koreatech.sw.kosp.domain.community.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.ArticleBookmark;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.ArticleLike;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.upload.repository.AttachmentRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleService 단위 테스트")
class ArticleServiceTest {

    @InjectMocks
    private ArticleService articleService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleLikeRepository articleLikeRepository;

    @Mock
    private ArticleBookmarkRepository articleBookmarkRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

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

    private Board createBoard(Long id, String name, boolean isNotice) {
        Board board = Board.builder()
            .name(name)
            .description(name + " 게시판")
            .build();
        ReflectionTestUtils.setField(board, "id", id);
        ReflectionTestUtils.setField(board, "isNotice", isNotice);
        return board;
    }

    private Article createArticle(Long id, User author, Board board, String title) {
        Article article = Article.builder()
            .author(author)
            .board(board)
            .title(title)
            .content("내용")
            .build();
        ReflectionTestUtils.setField(article, "id", id);
        ReflectionTestUtils.setField(article, "isDeleted", false);
        ReflectionTestUtils.setField(article, "createdAt", java.time.LocalDateTime.now());
        return article;
    }

    @Nested
    @DisplayName("create 메서드")
    class CreateTest {

        @Test
        @DisplayName("공지사항 게시판에 글 작성 시 예외가 발생한다")
        void throwsException_whenBoardIsNotice() {
            // given
            User user = createUser(1L, "작성자");
            Board noticeBoard = createBoard(1L, "공지사항", true);
            ArticleRequest request = new ArticleRequest(1L, "제목", "내용", List.of(), null);

            // when & then
            assertThatThrownBy(() -> articleService.create(user, noticeBoard, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("일반 게시판에 글을 성공적으로 작성한다")
        void createsArticle_whenBoardIsNormal() {
            // given
            User user = createUser(1L, "작성자");
            Board board = createBoard(1L, "자유게시판", false);
            ArticleRequest request = new ArticleRequest(1L, "제목", "내용", List.of("태그1"), null);
            
            Article savedArticle = createArticle(1L, user, board, "제목");
            given(articleRepository.save(any(Article.class))).willReturn(savedArticle);

            // when
            Long articleId = articleService.create(user, board, request);

            // then
            assertThat(articleId).isEqualTo(1L);
            verify(articleRepository).save(any(Article.class));
        }
    }

    @Nested
    @DisplayName("getOne 메서드")
    class GetOneTest {

        @Test
        @DisplayName("삭제된 게시글 조회 시 예외가 발생한다")
        void throwsException_whenArticleIsDeleted() {
            // given
            User user = createUser(1L, "조회자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, user, board, "삭제된 글");
            ReflectionTestUtils.setField(article, "isDeleted", true);
            given(articleRepository.getById(1L)).willReturn(article);

            // when & then
            assertThatThrownBy(() -> articleService.getOne(1L, user))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("게시글을 성공적으로 조회하고 조회수가 증가한다")
        void returnsArticleAndIncreasesViews() {
            // given
            User author = createUser(1L, "작성자");
            User viewer = createUser(2L, "조회자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, author, board, "테스트 글");
            ReflectionTestUtils.setField(article, "views", 0);
            
            given(articleRepository.getById(1L)).willReturn(article);
            given(articleLikeRepository.existsByUserAndArticle(viewer, article)).willReturn(false);
            given(articleBookmarkRepository.existsByUserAndArticle(viewer, article)).willReturn(false);

            // when
            ArticleResponse response = articleService.getOne(1L, viewer);

            // then
            assertThat(response.title()).isEqualTo("테스트 글");
            assertThat(article.getViews()).isEqualTo(1);
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
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, user, board, "글");
            ReflectionTestUtils.setField(article, "likes", 0);
            
            given(articleRepository.getById(1L)).willReturn(article);
            given(articleLikeRepository.findByUserAndArticle(user, article)).willReturn(Optional.empty());

            // when
            boolean result = articleService.toggleLike(user, 1L);

            // then
            assertThat(result).isTrue();
            assertThat(article.getLikes()).isEqualTo(1);
            verify(articleLikeRepository).save(any(ArticleLike.class));
        }

        @Test
        @DisplayName("좋아요가 있으면 제거한다")
        void removesLike_whenExists() {
            // given
            User user = createUser(1L, "사용자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, user, board, "글");
            ReflectionTestUtils.setField(article, "likes", 1);
            
            ArticleLike existingLike = ArticleLike.builder().user(user).article(article).build();
            
            given(articleRepository.getById(1L)).willReturn(article);
            given(articleLikeRepository.findByUserAndArticle(user, article)).willReturn(Optional.of(existingLike));

            // when
            boolean result = articleService.toggleLike(user, 1L);

            // then
            assertThat(result).isFalse();
            assertThat(article.getLikes()).isEqualTo(0);
            verify(articleLikeRepository).delete(existingLike);
        }
    }

    @Nested
    @DisplayName("toggleBookmark 메서드")
    class ToggleBookmarkTest {

        @Test
        @DisplayName("북마크가 없으면 추가한다")
        void addsBookmark_whenNotExists() {
            // given
            User user = createUser(1L, "사용자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, user, board, "글");
            
            given(articleRepository.getById(1L)).willReturn(article);
            given(articleBookmarkRepository.findByUserAndArticle(user, article)).willReturn(Optional.empty());

            // when
            boolean result = articleService.toggleBookmark(user, 1L);

            // then
            assertThat(result).isTrue();
            verify(articleBookmarkRepository).save(any(ArticleBookmark.class));
        }

        @Test
        @DisplayName("북마크가 있으면 제거한다")
        void removesBookmark_whenExists() {
            // given
            User user = createUser(1L, "사용자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, user, board, "글");
            ArticleBookmark bookmark = ArticleBookmark.builder().user(user).article(article).build();
            
            given(articleRepository.getById(1L)).willReturn(article);
            given(articleBookmarkRepository.findByUserAndArticle(user, article)).willReturn(Optional.of(bookmark));

            // when
            boolean result = articleService.toggleBookmark(user, 1L);

            // then
            assertThat(result).isFalse();
            verify(articleBookmarkRepository).delete(bookmark);
        }
    }

    @Nested
    @DisplayName("update 메서드")
    class UpdateTest {

        @Test
        @DisplayName("작성자가 아니면 수정 시 예외가 발생한다")
        void throwsException_whenNotOwner() {
            // given
            User author = createUser(1L, "작성자");
            User other = createUser(2L, "다른 사용자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, author, board, "글");
            ArticleRequest request = new ArticleRequest(1L, "수정 제목", "수정 내용", List.of(), null);
            
            given(articleRepository.getById(1L)).willReturn(article);

            // when & then
            assertThatThrownBy(() -> articleService.update(other, 1L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("작성자가 게시글을 성공적으로 수정한다")
        void updatesArticle_whenOwner() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, author, board, "기존 제목");
            ArticleRequest request = new ArticleRequest(1L, "수정 제목", "수정 내용", List.of("새태그"), null);
            
            given(articleRepository.getById(1L)).willReturn(article);

            // when
            articleService.update(author, 1L, request);

            // then
            assertThat(article.getTitle()).isEqualTo("수정 제목");
            assertThat(article.getContent()).isEqualTo("수정 내용");
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
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, author, board, "글");
            
            given(articleRepository.getById(1L)).willReturn(article);

            // when & then
            assertThatThrownBy(() -> articleService.delete(other, 1L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("작성자가 게시글을 성공적으로 삭제한다")
        void deletesArticle_whenOwner() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, author, board, "글");
            
            given(articleRepository.getById(1L)).willReturn(article);

            // when
            articleService.delete(author, 1L);

            // then
            verify(articleRepository).delete(article);
        }
    }

    @Nested
    @DisplayName("getList 메서드")
    class GetListTest {

        @Test
        @DisplayName("게시판의 게시글 목록을 페이징하여 조회한다")
        void returnsPagedArticleList() {
            // given
            User user = createUser(1L, "조회자");
            Board board = createBoard(1L, "자유게시판", false);
            Article article = createArticle(1L, user, board, "글");
            Pageable pageable = PageRequest.of(0, 10);
            Page<Article> page = new PageImpl<>(List.of(article), pageable, 1);
            
            given(articleRepository.findByBoardAndIsDeletedFalse(board, pageable)).willReturn(page);

            // when
            ArticleListResponse<ArticleResponse> result = articleService.getList(board, pageable, user);

            // then
            assertThat(result.posts()).hasSize(1);
        }
    }
}
