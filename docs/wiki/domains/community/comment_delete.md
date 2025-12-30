# 댓글 삭제 (Comment Delete)

## 📡 API Specification
**`DELETE /v1/community/articles/{articleId}/comments/{commentId}`**

*   **Description**: 본인이 작성한 댓글을 삭제합니다.
*   **Permission Name**: `comment:delete`
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
  "message": "본인의 댓글만 삭제할 수 있습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `CommentController.delete`
*   **Flow**:
1. 댓글 존재 여부 및 작성자 확인.
2. 부모 댓글인 경우 자식 댓글 처리 정책 적용 (삭제된 상태로 표시 or 연쇄 삭제).
3. Soft Delete 처리.
