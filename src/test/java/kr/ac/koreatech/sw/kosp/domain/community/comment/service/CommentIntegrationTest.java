package kr.ac.koreatech.sw.kosp.domain.community.comment.service;

import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.request.CommentCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.Comment;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private CommentRepository commentRepository;

    private MockHttpSession authorSession;
    private MockHttpSession otherUserSession;
    private Article testArticle;

    @BeforeEach
    void setup() throws Exception {
        createRole("ROLE_STUDENT");
        createGithubUser(1001L);
        createGithubUser(1002L);

        // 1. Create Author (게시글 & 댓글 작성자)
        String authorToken = createSignupToken(1001L, "author@koreatech.ac.kr");
        UserSignupRequest authorReq = new UserSignupRequest(
            "author", "2020001001", "author@koreatech.ac.kr", getValidPassword(), authorToken
        );
        userService.signup(authorReq);

        LoginRequest authorLogin = new LoginRequest("author@koreatech.ac.kr", getValidPassword());
        MvcResult authorResult = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorLogin)))
            .andExpect(status().isOk())
            .andReturn();
        authorSession = (MockHttpSession) authorResult.getRequest().getSession();

        // 2. Create Other User (다른 사용자)
        String otherToken = createSignupToken(1002L, "other@koreatech.ac.kr");
        UserSignupRequest otherReq = new UserSignupRequest(
            "other", "2020001002", "other@koreatech.ac.kr", getValidPassword(), otherToken
        );
        userService.signup(otherReq);

        LoginRequest otherLogin = new LoginRequest("other@koreatech.ac.kr", getValidPassword());
        MvcResult otherResult = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherLogin)))
            .andExpect(status().isOk())
            .andReturn();
        otherUserSession = (MockHttpSession) otherResult.getRequest().getSession();

        // 3. Create Board
        Board board = Board.builder()
            .name("Test Board")
            .description("Test Description")
            .isRecruitAllowed(false)
            .build();
        boardRepository.save(board);

        // 4. Create Article
        ArticleRequest articleReq = new ArticleRequest(
            board.getId(), "Test Article", "Test Content", List.of("TEST")
        );
        mockMvc.perform(post("/v1/community/articles")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articleReq)))
            .andExpect(status().isCreated());

        testArticle = articleRepository.findByTitleContaining("Test Article").get(0);
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void createComment_success() throws Exception {
        // given
        CommentCreateRequest req = new CommentCreateRequest("테스트 댓글입니다.");

        // when
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        // then
        List<Comment> comments = commentRepository.findByArticleId(testArticle.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("테스트 댓글입니다.");
        assertThat(comments.get(0).getAuthor().getKutEmail()).isEqualTo("author@koreatech.ac.kr");
    }

    @Test
    @DisplayName("댓글 작성 실패 - 빈 내용")
    void createComment_fail_emptyContent() throws Exception {
        // given
        CommentCreateRequest req = new CommentCreateRequest("");

        // when & then
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 존재하지 않는 게시글")
    void createComment_fail_articleNotFound() throws Exception {
        // given
        CommentCreateRequest req = new CommentCreateRequest("댓글 내용");

        // when & then
        mockMvc.perform(post("/v1/community/articles/99999/comments")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getComments_success() throws Exception {
        // given: 댓글 2개 작성
        CommentCreateRequest req1 = new CommentCreateRequest("첫 번째 댓글");
        CommentCreateRequest req2 = new CommentCreateRequest("두 번째 댓글");

        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(otherUserSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(authorSession))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comments").isArray())
            .andExpect(jsonPath("$.comments.length()").value(2))
            .andExpect(jsonPath("$.comments[0].content").value("첫 번째 댓글"))
            .andExpect(jsonPath("$.comments[1].content").value("두 번째 댓글"));
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 본인 댓글")
    void deleteComment_success_owner() throws Exception {
        // given: 댓글 작성
        CommentCreateRequest req = new CommentCreateRequest("삭제할 댓글");
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Comment comment = commentRepository.findByArticleId(testArticle.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when
        mockMvc.perform(delete("/v1/community/articles/" + testArticle.getId() + "/comments/" + comment.getId())
                .session(authorSession))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        List<Comment> comments = commentRepository.findByArticleId(testArticle.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 타인 댓글")
    void deleteComment_fail_notOwner() throws Exception {
        // given: author가 댓글 작성
        CommentCreateRequest req = new CommentCreateRequest("author의 댓글");
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Comment comment = commentRepository.findByArticleId(testArticle.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when & then: otherUser가 삭제 시도
        mockMvc.perform(delete("/v1/community/articles/" + testArticle.getId() + "/comments/" + comment.getId())
                .session(otherUserSession))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
    void deleteComment_fail_commentNotFound() throws Exception {
        // when & then
        mockMvc.perform(delete("/v1/community/articles/" + testArticle.getId() + "/comments/99999")
                .session(authorSession))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 좋아요 토글 성공 - On")
    void toggleCommentLike_success_on() throws Exception {
        // given: 댓글 작성
        CommentCreateRequest req = new CommentCreateRequest("좋아요 테스트 댓글");
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Comment comment = commentRepository.findByArticleId(testArticle.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // when: 좋아요 On
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments/" + comment.getId() + "/likes")
                .session(otherUserSession))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isLiked").value(true));
    }

    @Test
    @DisplayName("댓글 좋아요 토글 성공 - Off")
    void toggleCommentLike_success_off() throws Exception {
        // given: 댓글 작성 및 좋아요
        CommentCreateRequest req = new CommentCreateRequest("좋아요 취소 테스트");
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments")
                .session(authorSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        Comment comment = commentRepository.findByArticleId(testArticle.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().get(0);

        // 좋아요 On
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments/" + comment.getId() + "/likes")
                .session(otherUserSession))
            .andExpect(status().isOk());

        // when: 좋아요 Off
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments/" + comment.getId() + "/likes")
                .session(otherUserSession))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isLiked").value(false));
    }

    @Test
    @DisplayName("댓글 좋아요 실패 - 존재하지 않는 댓글")
    void toggleCommentLike_fail_commentNotFound() throws Exception {
        // when & then
        mockMvc.perform(post("/v1/community/articles/" + testArticle.getId() + "/comments/99999/likes")
                .session(authorSession))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}
