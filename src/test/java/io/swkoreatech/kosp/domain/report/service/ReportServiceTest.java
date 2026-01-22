package io.swkoreatech.kosp.domain.report.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.report.dto.request.ReportRequest;
import io.swkoreatech.kosp.domain.report.model.Report;
import io.swkoreatech.kosp.domain.report.model.enums.ReportReason;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;
import io.swkoreatech.kosp.domain.report.repository.ReportRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 단위 테스트")
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

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
        return article;
    }

    @Nested
    @DisplayName("reportArticle 메서드")
    class ReportArticleTest {

        @Test
        @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
        void throwsException_whenArticleNotFound() {
            // given
            User reporter = createUser(1L, "신고자");
            ReportRequest request = new ReportRequest(ReportReason.SPAM, "스팸입니다");
            given(articleRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reportService.reportArticle(reporter, 999L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("자신의 게시글을 신고하면 예외가 발생한다")
        void throwsException_whenSelfReport() {
            // given
            User author = createUser(1L, "작성자");
            Article article = createArticle(1L, author);
            ReportRequest request = new ReportRequest(ReportReason.SPAM, "스팸입니다");
            given(articleRepository.findById(1L)).willReturn(Optional.of(article));

            // when & then
            assertThatThrownBy(() -> reportService.reportArticle(author, 1L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("이미 신고한 게시글을 다시 신고하면 예외가 발생한다")
        void throwsException_whenAlreadyReported() {
            // given
            User author = createUser(1L, "작성자");
            User reporter = createUser(2L, "신고자");
            Article article = createArticle(1L, author);
            ReportRequest request = new ReportRequest(ReportReason.SPAM, "스팸입니다");
            
            given(articleRepository.findById(1L)).willReturn(Optional.of(article));
            given(reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, ReportTargetType.ARTICLE, 1L))
                .willReturn(true);

            // when & then
            assertThatThrownBy(() -> reportService.reportArticle(reporter, 1L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("게시글을 성공적으로 신고한다")
        void reportsArticle_successfully() {
            // given
            User author = createUser(1L, "작성자");
            User reporter = createUser(2L, "신고자");
            Article article = createArticle(1L, author);
            ReportRequest request = new ReportRequest(ReportReason.SPAM, "스팸입니다");
            
            given(articleRepository.findById(1L)).willReturn(Optional.of(article));
            given(reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, ReportTargetType.ARTICLE, 1L))
                .willReturn(false);

            // when
            reportService.reportArticle(reporter, 1L, request);

            // then
            verify(reportRepository).save(any(Report.class));
        }
    }
}
