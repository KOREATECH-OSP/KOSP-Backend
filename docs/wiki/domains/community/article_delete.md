# 게시글 삭제 (Article Delete)

## 📡 API Specification
**`DELETE /v1/community/articles/{id}`**

*   **Description**: 본인이 작성한 게시글을 삭제합니다.
*   **Permission Name**: `community:article:delete`
*   **Permissions**: `USER` (본인) or `ADMIN`

### Response
*   **204 No Content**
```json
// No Content
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
  "message": "권한이 없습니다 (본인 글만 삭제 가능)."
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
*   **Controller**: `ArticleController.delete`
*   **Service**: `ArticleService.delete`
*   **Flow**:
1. `ArticleRepository`에서 ID로 게시글 조회.
2. `validateOwner()`: 작성자 본인인지 확인.
3. `articleRepository.delete()` 호출 (Hard Delete).
