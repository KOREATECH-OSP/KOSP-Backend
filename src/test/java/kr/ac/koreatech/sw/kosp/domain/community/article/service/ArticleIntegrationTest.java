package kr.ac.koreatech.sw.kosp.domain.community.article.service;

import jakarta.servlet.http.HttpSession;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
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

class ArticleIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository userRepository;

    private MockHttpSession session;
    private MockHttpSession otherUserSession;
    private Board testBoard;
    private kr.ac.koreatech.sw.kosp.domain.user.model.User testUser;
    private kr.ac.koreatech.sw.kosp.domain.user.model.User otherUser;

    @BeforeEach
    void setup() throws Exception {
        createRole("ROLE_STUDENT");
        createGithubUser(999L);
        createGithubUser(888L);

        // 1. Signup & Login - Test User
        UserSignupRequest signupReq = new UserSignupRequest(
            "writer", "2020136999", "writer@koreatech.ac.kr", getValidPassword(), 999L
        );
        userService.signup(signupReq);
        testUser = userRepository.findByKutEmail("writer@koreatech.ac.kr").orElseThrow();

        LoginRequest loginReq = new LoginRequest("writer@koreatech.ac.kr", getValidPassword());
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andReturn();
        session = (MockHttpSession) result.getRequest().getSession();

        // 2. Signup & Login - Other User
        UserSignupRequest otherReq = new UserSignupRequest(
            "otherWriter", "2020136888", "other@koreatech.ac.kr", getValidPassword(), 888L
        );
        userService.signup(otherReq);
        otherUser = userRepository.findByKutEmail("other@koreatech.ac.kr").orElseThrow();

        LoginRequest otherLogin = new LoginRequest("other@koreatech.ac.kr", getValidPassword());
        MvcResult otherResult = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherLogin)))
            .andExpect(status().isOk())
            .andReturn();
        otherUserSession = (MockHttpSession) otherResult.getRequest().getSession();

        // 3. Create Board
        testBoard = Board.builder()
            .name("Free Board")
            .description("Free Talk")
            .isRecruitAllowed(false)
            .build();
        boardRepository.save(testBoard);
    }

    @Test
    @DisplayName("게시글 작성 성공")
    void createArticle_success() throws Exception {
        // given
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "My Title", "My Content", List.of("FREE"));

        // when
        mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isCreated());

        // then
        Article article = articleRepository.findByTitleContaining("My Title").get(0);
        assertThat(article.getTitle()).isEqualTo("My Title");
        assertThat(article.getContent()).isEqualTo("My Content");
        assertThat(article.getBoard().getId()).isEqualTo(testBoard.getId());
    }

    @Test
    @DisplayName("게시글 조회 성공")
    void getArticle_success() throws Exception {
        // given: Create article via API
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "Read Title", "Read Content", List.of("FREE"));
        mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
        Article article = articleRepository.findByTitleContaining("Read Title").get(0);

        // when
        mockMvc.perform(get("/v1/community/articles/" + article.getId())
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Read Title"));
    }

    @Test
    @DisplayName("좋아요 토글 (On)")
    void toggleLike_success() throws Exception {
        // given
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "Like Title", "Like Content", List.of("FREE"));
        mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
        Article article = articleRepository.findByTitleContaining("Like Title").get(0);

        // when: Like
        mockMvc.perform(post("/v1/community/articles/" + article.getId() + "/likes")
                .session(session))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("즐겨찾기 토글 (On)")
    void toggleBookmark_success() throws Exception {
        // given
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "Book Title", "Content", List.of("FREE"));
        mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
        Article article = articleRepository.findByTitleContaining("Book Title").get(0);

        // when
        mockMvc.perform(post("/v1/community/articles/" + article.getId() + "/bookmarks")
                .session(session))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getArticleDetail_success() throws Exception {
        // given
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "Detail Title", "Detail Content", List.of("TAG"));
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when & then
        mockMvc.perform(get("/v1/community/articles/" + articleId)
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Detail Title"))
            .andExpect(jsonPath("$.content").value("Detail Content"));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateArticle_success() throws Exception {
        // given
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Original", "Original Content", List.of("TAG"));
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when
        ArticleRequest updateReq = new ArticleRequest(testBoard.getId(), "Updated", "Updated Content", List.of("NEW"));
        mockMvc.perform(put("/v1/community/articles/" + articleId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isOk());

        // then
        Article updated = articleRepository.findById(articleId).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated");
        assertThat(updated.getContent()).isEqualTo("Updated Content");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 타인 게시글")
    void updateArticle_fail_notOwner() throws Exception {
        // given
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Original", "Content", List.of("TAG"));
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when & then
        ArticleRequest updateReq = new ArticleRequest(testBoard.getId(), "Hacked", "Hacked Content", List.of("HACK"));
        mockMvc.perform(put("/v1/community/articles/" + articleId)
                .session(otherUserSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteArticle_success() throws Exception {
        // given
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "To Delete", "Content", List.of("TAG"));
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when
        mockMvc.perform(delete("/v1/community/articles/" + articleId)
                .session(session))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        assertThat(articleRepository.findById(articleId)).isEmpty();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 타인 게시글")
    void deleteArticle_fail_notOwner() throws Exception {
        // given
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Protected", "Content", List.of("TAG"));
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when & then
        mockMvc.perform(delete("/v1/community/articles/" + articleId)
                .session(otherUserSession))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 좋아요 토글 - Off")
    void toggleLike_off() throws Exception {
        // given: 좋아요 On
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Unlike Me", "Content", List.of("TAG"));
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        mockMvc.perform(post("/v1/community/articles/" + articleId + "/likes")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isLiked").value(true));

        // when & then: 좋아요 Off
        mockMvc.perform(post("/v1/community/articles/" + articleId + "/likes")
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isLiked").value(false));
    }

    @Test
    @DisplayName("게시글 북마크 토글 - Off")
    void toggleBookmark_off() throws Exception {
        // given: 북마크 On
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Unbookmark Me", "Content", List.of("TAG"));
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        mockMvc.perform(post("/v1/community/articles/" + articleId + "/bookmarks")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isBookmarked").value(true));

        // when & then: 북마크 Off
        mockMvc.perform(post("/v1/community/articles/" + articleId + "/bookmarks")
                .session(session))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isBookmarked").value(false));
    }
}
