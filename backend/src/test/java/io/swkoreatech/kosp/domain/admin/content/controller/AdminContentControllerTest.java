package io.swkoreatech.kosp.domain.admin.content.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.auth.model.Permission;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.auth.repository.PermissionRepository;
import io.swkoreatech.kosp.domain.auth.repository.PolicyRepository;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.board.repository.BoardRepository;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.common.IntegrationTestSupport;

@DisplayName("AdminContentController 통합 테스트")
class AdminContentControllerTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private User adminUser;
    private User authorUser;
    private Board board;
    private Article article;
    private Comment comment;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Create GitHub user (required for User entity)
        createGithubUser(1001L);

        // 2. Create admin user with ROLE_ADMIN
        adminUser = User.builder()
            .name("관리자")
            .kutId("2024001")
            .kutEmail("admin@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(adminUser, "githubId", 1001L);
        adminUser = userRepository.save(adminUser);

        // 3. Assign ROLE_ADMIN (PermissionInitializer auto-creates this role in test env)
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found - PermissionInitializer didn't run"));
        adminUser.getRoles().add(adminRole);
        adminUser = userRepository.save(adminUser);

        // 3.5. Fallback A1: Ensure AdminPolicy includes admin:comments:delete (TEST ONLY)
        // Because authorization uses PermissionService.hasPermission() and traverses:
        // User -> Role -> Policy -> Permission

        // 3.5a. Fetch permission row (created by PermissionInitializer scanning @Permit)
        Permission deleteCommentPermission = permissionRepository.getByName("admin:comments:delete");

        // 3.5b. Fetch AdminPolicy (created by PermissionInitializer in clean H2 test env)
        Policy adminPolicy = policyRepository.getByName("AdminPolicy");

        // 3.5c. Add permission to AdminPolicy (idempotent)
        Set<Permission> updatedPermissions = new HashSet<>(adminPolicy.getPermissions());
        updatedPermissions.add(deleteCommentPermission);
        adminPolicy.updatePermissions(updatedPermissions);
        policyRepository.save(adminPolicy);

        // 4. Create author user (for Article/Comment)
        createGithubUser(1002L);
        authorUser = User.builder()
            .name("작성자")
            .kutId("2024002")
            .kutEmail("author@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(authorUser, "githubId", 1002L);
        authorUser = userRepository.save(authorUser);

        // 5. Create Board (required for Article)
        board = Board.builder()
            .name("테스트게시판")
            .description("테스트용")
            .build();
        board = boardRepository.save(board);

        // 6. Create Article (required for Comment)
        article = Article.builder()
            .author(authorUser)
            .board(board)
            .title("테스트 게시글")
            .content("테스트 내용")
            .build();
        article = articleRepository.save(article);

        // 7. Create Comment
        comment = Comment.builder()
            .author(authorUser)
            .article(article)
            .content("테스트 댓글")
            .build();
        comment = commentRepository.save(comment);

        // 8. Create admin token
        adminToken = createAccessToken(adminUser);
    }

    @Test
    @DisplayName("댓글을 성공적으로 삭제한다")
    void deleteComment_성공() throws Exception {
        mockMvc.perform(delete("/v1/admin/comments/{commentId}", comment.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        // Verify soft delete using findById (after Task 1 adds it)
        Comment deleted = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 404 응답")
    void deleteComment_존재하지않음_404() throws Exception {
        mockMvc.perform(delete("/v1/admin/comments/{commentId}", 99999L)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이미 삭제된 댓글 삭제 시 404 응답")
    void deleteComment_이미삭제됨_404() throws Exception {
        // Given: Already deleted comment
        comment.delete();
        commentRepository.save(comment);

        // When/Then: 404 because repository.getById() filters by isDeletedFalse
        mockMvc.perform(delete("/v1/admin/comments/{commentId}", comment.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("권한 없는 사용자는 403 응답")
    void deleteComment_권한없음_403() throws Exception {
        String nonAdminToken = createAccessToken(authorUser);

        mockMvc.perform(delete("/v1/admin/comments/{commentId}", comment.getId())
                .header("Authorization", "Bearer " + nonAdminToken))
            .andExpect(status().isForbidden());
    }
}
