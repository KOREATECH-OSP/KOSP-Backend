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

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "권한이 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `ArticleController.delete`
*   **Flow**:
1. 게시글 존재 여부 및 작성자 확인.
2. Soft Delete (`is_deleted = true`) 처리.
3. 연관된 댓글 등도 함께 처리할지 정책 결정.
