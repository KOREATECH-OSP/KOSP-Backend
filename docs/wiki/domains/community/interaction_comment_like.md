# 댓글 좋아요 (Comment Like)

## 📡 API Specification
**`POST /v1/community/articles/{articleId}/comments/{commentId}/likes`**
*(Note: 실제 경로는 `/v1/community/articles/{articleId}/comments/{commentId}/likes` 형식을 따름)*

*   **Description**: 댓글 좋아요 상태를 토글(Toggle)합니다.
*   **Permission Name**: `comment:like`
*   **Permissions**: `USER`

### Response
*   **200 OK**
```json
{
  "liked": true,
  "count": 5
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `CommentController.toggleLike`
*   **Flow**:
1. `CommentRepository` 댓글 존재 확인.
2. `CommentLikeRepository` 상태 확인 및 토글 (Article Like와 유사 로직).
