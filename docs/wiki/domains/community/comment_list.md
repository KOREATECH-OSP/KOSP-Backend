# 댓글 목록 조회 (Comment List)

## 📡 API Specification
**`GET /v1/community/articles/{articleId}/comments`**

*   **Description**: 게시글의 모든 댓글을 조회합니다. 계층 구조는 클라이언트 또는 서버에서 처리합니다.
*   **Permission Name**: `comment:list`
*   **Permissions**: `ANONYMOUS` (or `USER`)

### Response
*   **200 OK**
```json
{
  "comments": [
    {
      "id": 100,
      "content": "좋은 글이네요.",
      "author": { "nickname": "김철수" },
      "createdAt": "2024-12-30T10:05:00"
    }
  ],
  "meta": {
    "page": 1,
    "size": 10,
    "totalCount": 5,
    "totalPages": 1
  }
}
```

*   **404 Not Found**
```json
{
  "code": "ARTICLE_NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `CommentController.getList`
*   **Service**: `CommentService.getList`
*   **Flow**:
1. `ArticleRepository`에서 게시글 존재 여부 확인 (Optional, Repository 레벨에서 처리 가능).
2. `CommentRepository.findByArticleId`로 댓글 목록 조회.
3. 각 댓글에 대해 `isLiked`, `isMine` 여부 확인하여 응답 구성.
