package io.swkoreatech.kosp.domain.community.article.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;
import lombok.Builder;

/**
 * 게시글 상세 응답 DTO.
 *
 * @param id 게시글 ID
 * @param boardId 게시판 ID
 * @param title 제목
 * @param content 내용
 * @param author 작성자 정보
 * @param views 조회수
 * @param likes 좋아요 수
 * @param comments 댓글 수
 * @param tags 태그 목록
 * @param isLiked 현재 사용자의 좋아요 여부
 * @param isBookmarked 현재 사용자의 북마크 여부
 * @param isPinned 고정 게시글 여부
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 */
@Builder
public record ArticleResponse(
    Long id,
    Long boardId,
    String title,
    String content,
    AuthorResponse author,
    Integer views,
    Integer likes,
    Integer comments,
    List<String> tags,
    Boolean isLiked,
    Boolean isBookmarked,
    Boolean isPinned,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * 게시글 엔티티로부터 응답 객체를 생성한다.
     *
     * @param article 게시글 엔티티
     * @param isLiked 좋아요 여부
     * @param isBookmarked 북마크 여부
     * @return 게시글 응답
     */
    public static ArticleResponse from(Article article, boolean isLiked, boolean isBookmarked) {
        return ArticleResponse.builder()
            .id(article.getId())
            .boardId(article.getBoardId())
            .title(article.getTitle())
            .content(article.getContent())
            .author(AuthorResponse.from(article.getAuthor()))
            .views(article.getViews())
            .likes(article.getLikes())
            .comments(article.getCommentsCount())
            .tags(new java.util.ArrayList<>(article.getTags()))
            .isLiked(isLiked)
            .isBookmarked(isBookmarked)
            .isPinned(article.isPinned())
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .build();
    }
}
