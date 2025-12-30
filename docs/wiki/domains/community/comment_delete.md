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
  "message": "권한이 없습니다 (본인 댓글만 삭제 가능)."
}
```

*   **404 Not Found**
```json
{
  "code": "COMMENT_NOT_FOUND",
  "message": "댓글을 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `CommentController.delete`
*   **Service**: `CommentService.delete`
*   **Flow**:
1. `CommentRepository`에서 댓글 조회.
2. `validateOwner()`: 댓글 작성자 본인인지 확인.
3. `commentRepository.delete()` 호출.
2. 부모 댓글인 경우 자식 댓글 처리 정책 적용 (삭제된 상태로 표시 or 연쇄 삭제).
3. Soft Delete 처리.
