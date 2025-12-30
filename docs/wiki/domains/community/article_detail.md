# 게시글 상세 조회 (Article Detail)

## 📡 API Specification
**`GET /v1/community/articles/{id}`**

*   **Description**: 게시글의 상세 내용을 조회합니다.
*   **Permission Name**: `community:article:read`
*   **Permissions**: `ANONYMOUS` (or `USER` depending on board policy)

### Response
*   **200 OK**
```json
{
  "id": 10,
  "title": "Spring Boot 질문있습니다",
  "content": "JPA N+1 문제...",
  "author": { "id": 1, "nickname": "홍길동" },
  "views": 16,
  "likes": 3,
  "commentCount": 5,
  "createdAt": "2024-12-30T10:00:00",
  "tags": ["Spring", "JPA"],
  "images": ["..."]
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
*   **Controller**: `ArticleController.getOne`
*   **Service**: `ArticleService.getOne`
*   **Flow**:
1. `ArticleRepository`에서 ID로 게시글 조회 (없을 시 404).
2. 조회수 증가 (`article.increaseViews()`).
3. 로그인한 사용자의 `isLiked`, `isBookmarked` 상태 확인.
4. 응답 DTO 반환.
