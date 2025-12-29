package kr.ac.koreatech.sw.kosp.domain.admin.dto.response;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public record AdminSearchResponse(
    List<UserSummary> users,
    List<ArticleSummary> articles
) {
    public record UserSummary(Long id, String name, String email, String profileImage) {
        public static UserSummary from(User user) {
            String profileImage = user.getGithubUser() != null ? user.getGithubUser().getGithubAvatarUrl() : null;
            return new UserSummary(user.getId(), user.getName(), user.getKutEmail(), profileImage);
        }
    }

    public record ArticleSummary(Long id, String title, String boardName, String authorName, String createdAt) {
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
