package kr.ac.koreatech.sw.kosp.domain.admin.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.ac.koreatech.sw.kosp.domain.admin.search.dto.response.AdminSearchResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminSearchService 단위 테스트")
class AdminSearchServiceTest {

    @InjectMocks
    private AdminSearchService adminSearchService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArticleRepository articleRepository;

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

    private Board createBoard(Long id, String name) {
        Board board = Board.builder()
            .name(name)
            .description(name + " 게시판")
            .build();
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }

    private Article createArticle(Long id, String title) {
        User author = createUser(id, "author" + id);
        Board board = createBoard(id, "board" + id);
        Article article = Article.builder()
            .title(title)
            .content("테스트 내용")
            .author(author)
            .board(board)
            .build();
        ReflectionTestUtils.setField(article, "id", id);
        ReflectionTestUtils.setField(article, "createdAt", java.time.LocalDateTime.now());
        return article;
    }

    @Nested
    @DisplayName("search 메서드")
    class SearchTest {

        @Test
        @DisplayName("키워드가 null이면 빈 결과를 반환한다")
        void returnsEmptyResult_whenKeywordIsNull() {
            // when
            AdminSearchResponse result = adminSearchService.search(null, "ALL");

            // then
            assertThat(result.users()).isEmpty();
            assertThat(result.articles()).isEmpty();
            verify(userRepository, never()).findByNameContaining(anyString());
            verify(articleRepository, never()).findByTitleContaining(anyString());
        }

        @Test
        @DisplayName("키워드가 빈 문자열이면 빈 결과를 반환한다")
        void returnsEmptyResult_whenKeywordIsEmpty() {
            // when
            AdminSearchResponse result = adminSearchService.search("", "ALL");

            // then
            assertThat(result.users()).isEmpty();
            assertThat(result.articles()).isEmpty();
        }

        @Test
        @DisplayName("키워드가 공백만 있으면 빈 결과를 반환한다")
        void returnsEmptyResult_whenKeywordIsBlank() {
            // when
            AdminSearchResponse result = adminSearchService.search("   ", "ALL");

            // then
            assertThat(result.users()).isEmpty();
            assertThat(result.articles()).isEmpty();
        }

        @Test
        @DisplayName("타입이 USER이면 사용자만 검색한다")
        void searchesOnlyUsers_whenTypeIsUser() {
            // given
            User user = createUser(1L, "홍길동");
            given(userRepository.findByNameContaining("홍")).willReturn(List.of(user));

            // when
            AdminSearchResponse result = adminSearchService.search("홍", "USER");

            // then
            assertThat(result.users()).hasSize(1);
            assertThat(result.articles()).isEmpty();
            verify(articleRepository, never()).findByTitleContaining(anyString());
        }

        @Test
        @DisplayName("타입이 ARTICLE이면 게시글만 검색한다")
        void searchesOnlyArticles_whenTypeIsArticle() {
            // given
            Article article = createArticle(1L, "공지사항");
            given(articleRepository.findByTitleContaining("공지")).willReturn(List.of(article));

            // when
            AdminSearchResponse result = adminSearchService.search("공지", "ARTICLE");

            // then
            assertThat(result.users()).isEmpty();
            assertThat(result.articles()).hasSize(1);
            verify(userRepository, never()).findByNameContaining(anyString());
        }

        @Test
        @DisplayName("타입이 ALL이면 사용자와 게시글 모두 검색한다")
        void searchesBoth_whenTypeIsAll() {
            // given
            User user = createUser(1L, "테스트");
            Article article = createArticle(1L, "테스트 게시글");
            given(userRepository.findByNameContaining("테스트")).willReturn(List.of(user));
            given(articleRepository.findByTitleContaining("테스트")).willReturn(List.of(article));

            // when
            AdminSearchResponse result = adminSearchService.search("테스트", "ALL");

            // then
            assertThat(result.users()).hasSize(1);
            assertThat(result.articles()).hasSize(1);
        }

        @Test
        @DisplayName("타입이 null이면 사용자와 게시글 모두 검색한다")
        void searchesBoth_whenTypeIsNull() {
            // given
            given(userRepository.findByNameContaining("검색어")).willReturn(Collections.emptyList());
            given(articleRepository.findByTitleContaining("검색어")).willReturn(Collections.emptyList());

            // when
            AdminSearchResponse result = adminSearchService.search("검색어", null);

            // then
            verify(userRepository).findByNameContaining("검색어");
            verify(articleRepository).findByTitleContaining("검색어");
        }

        @Test
        @DisplayName("타입이 소문자여도 정상 동작한다")
        void worksWithLowercaseType() {
            // given
            User user = createUser(1L, "이름");
            given(userRepository.findByNameContaining("이름")).willReturn(List.of(user));

            // when
            AdminSearchResponse result = adminSearchService.search("이름", "user");

            // then
            assertThat(result.users()).hasSize(1);
        }

        @Test
        @DisplayName("검색 결과가 없으면 빈 리스트를 반환한다")
        void returnsEmptyLists_whenNoResults() {
            // given
            given(userRepository.findByNameContaining("없는이름")).willReturn(Collections.emptyList());
            given(articleRepository.findByTitleContaining("없는이름")).willReturn(Collections.emptyList());

            // when
            AdminSearchResponse result = adminSearchService.search("없는이름", "ALL");

            // then
            assertThat(result.users()).isEmpty();
            assertThat(result.articles()).isEmpty();
        }
    }
}
