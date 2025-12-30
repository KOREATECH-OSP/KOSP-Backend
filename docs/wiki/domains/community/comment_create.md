# 댓글 작성 (Comment Create)

## 📡 API Specification
**`POST /v1/community/articles/{articleId}/comments`**

*   **Description**: 게시글에 새 댓글을 작성합니다.
*   **Permission Name**: `comment:create`
*   **Permissions**: `USER`

### Request
```json
{
  "parentId": null,
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

---

## 🛠️ Implementation Details
*   **Controller**: `CommentController.create`
*   **Flow**:
1. `ArticleRepository`에서 게시글 존재 여부 확인.
2. `parentId` 존재 시 부모 댓글 확인 (대댓글).
3. `Comment` 엔티티 생성 및 저장.
4. `Location` 헤더 설정 및 반환.
