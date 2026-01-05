package kr.ac.koreatech.sw.kosp.domain.community.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.common.IntegrationTestSupport;

class ArticleIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private BoardRepository boardRepository;

    private String accessToken;
    private String otherUserAccessToken;
    private Board testBoard;

    @BeforeEach
    void setup() throws Exception {
        createGithubUser(999L);
        createGithubUser(888L);

        // 1. Signup & Login - Test User
        String writerToken = createSignupToken(999L, "writer@koreatech.ac.kr");
        UserSignupRequest signupRequest = new UserSignupRequest(
            "writer", "2020136999", "writer@koreatech.ac.kr", getValidPassword(), writerToken
        );
        userService.signup(signupRequest);
        
        accessToken = loginAndGetToken("writer@koreatech.ac.kr", getValidPassword());

        // 2. Signup & Login - Other User
        String otherToken = createSignupToken(888L, "other@koreatech.ac.kr");
        UserSignupRequest otherRequest = new UserSignupRequest(
            "otherWriter", "2020136888", "other@koreatech.ac.kr", getValidPassword(), otherToken
        );
        userService.signup(otherRequest);
        
        otherUserAccessToken = loginAndGetToken("other@koreatech.ac.kr", getValidPassword());

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
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "My Title", "My Content", List.of("FREE"), null);

        // when
        mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
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
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "Read Title", "Read Content", List.of("FREE"), null);
        mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
        Article article = articleRepository.findByTitleContaining("Read Title").get(0);

        // when
        mockMvc.perform(get("/v1/community/articles/" + article.getId())
                .header("Authorization", bearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Read Title"));
    }

    @Test
    @DisplayName("좋아요 토글 (On)")
    void toggleLike_success() throws Exception {
        // given
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "Like Title", "Like Content", List.of("FREE"), null);
        mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
        Article article = articleRepository.findByTitleContaining("Like Title").get(0);

        // when: Like
        mockMvc.perform(post("/v1/community/articles/" + article.getId() + "/likes")
                .header("Authorization", bearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("즐겨찾기 토글 (On)")
    void toggleBookmark_success() throws Exception {
        // given
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "Book Title", "Content", List.of("FREE"), null);
        mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
        Article article = articleRepository.findByTitleContaining("Book Title").get(0);

        // when
        mockMvc.perform(post("/v1/community/articles/" + article.getId() + "/bookmarks")
                .header("Authorization", bearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getArticleDetail_success() throws Exception {
        // given
        ArticleRequest req = new ArticleRequest(testBoard.getId(), "Detail Title", "Detail Content", List.of("TAG"), null);
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when & then
        mockMvc.perform(get("/v1/community/articles/" + articleId)
                .header("Authorization", bearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Detail Title"))
            .andExpect(jsonPath("$.content").value("Detail Content"));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateArticle_success() throws Exception {
        // given
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Original", "Original Content", List.of("TAG"), null);
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when
        ArticleRequest updateReq = new ArticleRequest(testBoard.getId(), "Updated", "Updated Content", List.of("NEW"), null);
        mockMvc.perform(put("/v1/community/articles/" + articleId)
                .header("Authorization", bearerToken(accessToken))
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
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Original", "Content", List.of("TAG"), null);
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when & then
        ArticleRequest updateReq = new ArticleRequest(testBoard.getId(), "Hacked", "Hacked Content", List.of("HACK"), null);
        mockMvc.perform(put("/v1/community/articles/" + articleId)
                .header("Authorization", bearerToken(otherUserAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteArticle_success() throws Exception {
        // given
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "To Delete", "Content", List.of("TAG"), null);
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when
        mockMvc.perform(delete("/v1/community/articles/" + articleId)
                .header("Authorization", bearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isNoContent());

        // then
        assertThat(articleRepository.findById(articleId)).isEmpty();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 타인 게시글")
    void deleteArticle_fail_notOwner() throws Exception {
        // given
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Protected", "Content", List.of("TAG"), null);
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when & then
        mockMvc.perform(delete("/v1/community/articles/" + articleId)
                .header("Authorization", bearerToken(otherUserAccessToken)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 좋아요 토글 - Off")
    void toggleLike_off() throws Exception {
        // given: 좋아요 On
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Unlike Me", "Content", List.of("TAG"), null);
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        mockMvc.perform(post("/v1/community/articles/" + articleId + "/likes")
                .header("Authorization", bearerToken(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isLiked").value(true));

        // when & then: 좋아요 Off
        mockMvc.perform(post("/v1/community/articles/" + articleId + "/likes")
                .header("Authorization", bearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isLiked").value(false));
    }

    @Test
    @DisplayName("게시글 북마크 토글 - Off")
    void toggleBookmark_off() throws Exception {
        // given: 북마크 On
        ArticleRequest createReq = new ArticleRequest(testBoard.getId(), "Unbookmark Me", "Content", List.of("TAG"), null);
        MvcResult createResult = mockMvc.perform(post("/v1/community/articles")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        Long articleId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        mockMvc.perform(post("/v1/community/articles/" + articleId + "/bookmarks")
                .header("Authorization", bearerToken(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isBookmarked").value(true));

        // when & then: 북마크 Off
        mockMvc.perform(post("/v1/community/articles/" + articleId + "/bookmarks")
                .header("Authorization", bearerToken(accessToken)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isBookmarked").value(false));
    }
}
