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
  "content": [
    {
      "id": 100,
      "content": "좋은 글이네요.",
      "author": { "nickname": "김철수" },
      "createdAt": "2024-12-30T10:05:00",
      "children": []
    }
  ],
  "pageable": { ... },
  "totalElements": 5
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `CommentController.getList`
*   **Flow**:
1. `ArticleRepository` 게시글 존재 확인.
2. `CommentRepository`에서 해당 게시글 댓글 페이징 조회.
3. `hibernate.default_batch_fetch_size` 설정을 통해 N+1 문제 최적화.
