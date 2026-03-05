package io.swkoreatech.kosp.domain.user.service;


import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleResponse;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleLikeRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentListResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentResponse;
import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentLikeRepository;
import io.swkoreatech.kosp.domain.community.comment.repository.CommentRepository;
import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;
import io.swkoreatech.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import io.swkoreatech.kosp.domain.user.dto.response.GithubActivityResponse;
import io.swkoreatech.kosp.global.dto.PageMeta;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 활동 조회 서비스.
 * 사용자의 게시글, 즐겨찾기, 댓글, GitHub 활동 내역 조회 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;
    private final GithubRepositoryStatisticsRepository repositoryStatisticsRepository;

    /**
     * 사용자가 작성한 게시글 목록을 조회한다.
     *
     * @param userId 조회 대상 사용자 ID
     * @param pageable 페이지 정보
     * @param user 인증된 사용자 (좋아요/즐겨찾기 여부 확인용, null 가능)
     * @return 게시글 목록 응답
     */
    public ArticleListResponse getPosts(Long userId, Pageable pageable, User user) {
        User author = User.builder().id(userId).build();
        Page<Article> page = articleRepository.findByAuthor(author, pageable);

        return toArticleResponse(page, user);
    }

    /**
     * 사용자가 즐겨찾기한 게시글 목록을 조회한다.
     *
     * @param userId 조회 대상 사용자 ID
     * @param pageable 페이지 정보
     * @param user 인증된 사용자 (좋아요/즐겨찾기 여부 확인용, null 가능)
     * @return 즐겨찾기 게시글 목록 응답
     */
    public ArticleListResponse getBookmarks(Long userId, Pageable pageable, User user) {
        Page<Article> page = articleBookmarkRepository.findArticlesByUserId(userId, pageable);
        return toArticleResponse(page, user);
    }

    /**
     * 사용자가 작성한 댓글 목록을 조회한다.
     *
     * @param userId 조회 대상 사용자 ID
     * @param pageable 페이지 정보
     * @param user 인증된 사용자 (좋아요 여부 확인용, null 가능)
     * @return 댓글 목록 응답
     */
    public CommentListResponse getComments(Long userId, Pageable pageable, User user) {
        Page<Comment> page = commentRepository.findByAuthorIdAndIsDeletedFalse(userId, pageable);
        return toCommentResponse(page, user);
    }

    /**
     * 사용자의 GitHub 활동 내역을 조회한다.
     * GitHub 계정이 연동되지 않은 경우 빈 응답을 반환한다.
     *
     * @param userId 조회 대상 사용자 ID
     * @return GitHub 활동 응답
     */
    public GithubActivityResponse getGithubActivities(Long userId) {
        User targetUser = userRepository.getById(userId);

        if (targetUser.getGithubUser() == null) {
            return GithubActivityResponse.empty();
        }

        String githubId = String.valueOf(targetUser.getGithubUser().getGithubId());
        List<GithubRepositoryStatistics> repositories = repositoryStatisticsRepository
            .findByContributorGithubIdOrderByLastCommitDateDesc(githubId);

        List<GithubActivityResponse.Activity> activities = repositories.stream()
            .map(repo -> new GithubActivityResponse.Activity(
                String.valueOf(repo.getId()),
                "REPOSITORY",
                repo.getRepoOwner() + "/" + repo.getRepoName(),
                repo.getDescription(),
                repo.getLastCommitDate() != null ? repo.getLastCommitDate().toString() : null,
                "https://github.com/" + repo.getRepoOwner() + "/" + repo.getRepoName()
            ))
            .toList();

        return new GithubActivityResponse(activities);
    }

    private ArticleListResponse toArticleResponse(Page<Article> page, User user) {
        List<ArticleResponse> posts = page.getContent().stream()
            .map(article -> ArticleResponse.from(
                article,
                isArticleLiked(user, article),
                isArticleBookmarked(user, article)
            ))
            .toList();
        return new ArticleListResponse<>(posts, PageMeta.from(page));
    }

    private CommentListResponse toCommentResponse(Page<Comment> page, User user) {
        List<CommentResponse> comments = page.getContent().stream()
            .map(comment -> CommentResponse.from(
                comment,
                isCommentLiked(user, comment),
                isCommentMine(user, comment)
            ))
            .toList();
        return CommentListResponse.from(comments, page);
    }

    private boolean isArticleLiked(User user, Article article) {
        return user != null && articleLikeRepository.existsByUserAndArticle(user, article);
    }

    private boolean isArticleBookmarked(User user, Article article) {
        return user != null && articleBookmarkRepository.existsByUserAndArticle(user, article);
    }

    private boolean isCommentLiked(User user, Comment comment) {
        return user != null && commentLikeRepository.existsByUserAndComment(user, comment);
    }

    private boolean isCommentMine(User user, Comment comment) {
        return user != null && comment.getAuthor().getId().equals(user.getId());
    }
}
