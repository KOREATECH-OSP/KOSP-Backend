# 댓글 작성 (Comment Create)

## 📡 API Specification
**`POST /v1/community/articles/{articleId}/comments`**

*   **Description**: 게시글에 새 댓글을 작성합니다.
*   **Permission Name**: `comment:create`
*   **Permissions**: `USER`

### Request
```json
{
  "content": "Fetch Join을 사용해보세요."
}
```

### Response
*   **201 Created**
    *   Headers: `Location: /v1/community/articles/{articleId}/comments/{id}`
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "내용은 필수입니다."
}
```

*   **401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다."
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
*   **Controller**: `CommentController.create`
*   **Service**: `CommentService.create`
*   **Flow**:
1. `ArticleRepository`에서 게시글 조회 (없을 시 404).
2. `Comment` 엔티티 생성 (Author, Content 설정).
3. `CommentRepository.save()` 호출.
4. `Location` 헤더 설정 및 반환.
