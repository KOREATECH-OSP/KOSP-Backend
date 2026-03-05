package io.swkoreatech.kosp.domain.admin.search.dto.response;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.model.Article;

import java.util.List;

/**
 * 관리자 통합 검색 응답 DTO.
 *
 * @param users    검색된 사용자 요약 목록
 * @param articles 검색된 게시글 요약 목록
 */
public record AdminSearchResponse(
    List<UserSummary> users,
    List<ArticleSummary> articles
) {
    /**
     * 사용자 및 게시글 검색 결과로부터 응답 DTO를 생성한다.
     *
     * @param users    사용자 검색 결과 목록
     * @param articles 게시글 검색 결과 목록
     * @return 관리자 통합 검색 응답 DTO
     */
    public static AdminSearchResponse from(List<UserSummary> users, List<ArticleSummary> articles) {
        return new AdminSearchResponse(users, articles);
    }

    /**
     * 빈 검색 결과를 반환한다.
     *
     * @return 빈 검색 응답 DTO
     */
    public static AdminSearchResponse empty() {
        return new AdminSearchResponse(java.util.Collections.emptyList(), java.util.Collections.emptyList());
    }

    /**
     * 사용자 요약 정보.
     *
     * @param id           사용자 식별자
     * @param name         사용자 이름
     * @param email        이메일
     * @param profileImage 프로필 이미지 URL
     */
    public record UserSummary(Long id, String name, String email, String profileImage) {
        /**
         * {@link User} 엔티티로부터 사용자 요약 정보를 생성한다.
         *
         * @param user 사용자 엔티티
         * @return 사용자 요약 정보
         */
        public static UserSummary from(User user) {
            String profileImage = user.getGithubUser() != null ? user.getGithubUser().getGithubAvatarUrl() : null;
            return new UserSummary(user.getId(), user.getName(), user.getKutEmail(), profileImage);
        }
    }

    /**
     * 게시글 요약 정보.
     *
     * @param id         게시글 식별자
     * @param title      게시글 제목
     * @param boardName  게시판 이름
     * @param authorName 작성자 이름
     * @param createdAt  작성 일시
     */
    public record ArticleSummary(Long id, String title, String boardName, String authorName, String createdAt) {
        /**
         * {@link Article} 엔티티로부터 게시글 요약 정보를 생성한다.
         *
         * @param article 게시글 엔티티
         * @return 게시글 요약 정보
         */
        public static ArticleSummary from(Article article) {
            return new ArticleSummary(
                article.getId(),
                article.getTitle(),
                article.getBoard().getName(),
                article.getAuthor().getName(),
                article.getCreatedAt().toString()
            );
        }
    }
}
