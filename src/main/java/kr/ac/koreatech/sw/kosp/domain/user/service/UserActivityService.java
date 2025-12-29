package kr.ac.koreatech.sw.kosp.domain.user.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.Comment;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.comment.repository.CommentRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;
import lombok.RequiredArgsConstructor;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubRepositoryRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.GithubActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubRepository;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final UserRepository userRepository;

    public ArticleListResponse getPosts(Long userId, Pageable pageable, User user) {
        User author = User.builder().id(userId).build();
        Page<Article> page = articleRepository.findByAuthor(author, pageable);
        
        return toArticleResponse(page, user);
    }

    public ArticleListResponse getBookmarks(Long userId, Pageable pageable, User user) {
        Page<Article> page = articleBookmarkRepository.findArticlesByUserId(userId, pageable);
        return toArticleResponse(page, user);
    }

    public CommentListResponse getComments(Long userId, Pageable pageable, User user) {
        Page<Comment> page = commentRepository.findByAuthorId(userId, pageable);
        return toCommentResponse(page, user);
    }

    public GithubActivityResponse getGithubActivities(Long userId) {
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));

        if (targetUser.getGithubUser() == null) {
            return new GithubActivityResponse(Collections.emptyList());
        }

        Long githubId = targetUser.getGithubUser().getGithubId();
        List<GithubRepository> repos = githubRepositoryRepository.findByOwnerIdOrderByCodeVolumeTotalCommitsDesc(githubId);

        List<GithubActivityResponse.Activity> activities = repos.stream()
            .map(this::mapToActivity)
            .toList();

        return new GithubActivityResponse(activities);
    }

    private GithubActivityResponse.Activity mapToActivity(GithubRepository repo) {
        String date = repo.getDates() != null && repo.getDates().getPushedAt() != null 
            ? repo.getDates().getPushedAt().toString() 
            : null;
            
        return new GithubActivityResponse.Activity(
            repo.getId(),
            "REPOSITORY",
            repo.getName(),
            repo.getDescription(),
            date,
            repo.getUrl()
        );
    }

    private ArticleListResponse toArticleResponse(Page<Article> page, User user) {
        List<ArticleResponse> posts = page.getContent().stream()
            .map(article -> ArticleResponse.from(
                article, 
                isArticleLiked(user, article), 
                isArticleBookmarked(user, article)
            ))
            .toList();
        return new ArticleListResponse(posts, PageMeta.from(page));
    }

    private CommentListResponse toCommentResponse(Page<Comment> page, User user) {
        List<CommentResponse> comments = page.getContent().stream()
            .map(comment -> CommentResponse.from(
                comment,
                isCommentLiked(user, comment),
                isCommentMine(user, comment)
            ))
            .toList();
        return new CommentListResponse(comments, PageMeta.from(page));
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
