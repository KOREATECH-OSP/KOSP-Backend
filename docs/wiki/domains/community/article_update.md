# 게시글 수정 (Article Update)

## 📡 API Specification
**`PUT /v1/community/articles/{id}`**

*   **Description**: 본인이 작성한 게시글을 수정합니다.
*   **Permission Name**: `community:article:update`
*   **Permissions**: `USER` (본인)

### Request
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "tags": ["Spring", "JPA", "Edit"],
  "images": []
}
```

### Response
*   **200 OK**
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "제목은 필수입니다."
}
```

*   **401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다."
}
```

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "권한이 없습니다 (본인 글만 수정 가능)."
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
*   **Controller**: `ArticleController.update`
*   **Service**: `ArticleService.update`
*   **Flow**:
1. `ArticleRepository`에서 ID로 게시글 조회 (없을 시 404).
2. `validateOwner()`: 요청한 사가 작성자인지 확인 (아닐 경우 403 Forbidden).
3. `article.updateArticle()` 호출하여 엔티티 수정.
