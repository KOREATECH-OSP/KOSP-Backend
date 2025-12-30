# 게시글 좋아요 (Article Like)

## 📡 API Specification
**`POST /v1/community/articles/{id}/likes`**

*   **Description**: 게시글 좋아요 상태를 토글(Toggle)합니다.
*   **Permission Name**: `community:article:like`
*   **Permissions**: `USER`

### Response
*   **200 OK**
```json
{
  "isLiked": true
}
```

*   **401 Unauthorized** (비로그인)
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
*   **Controller**: `ArticleController.toggleLike`
*   **Flow**:
1. `ArticleRepository` 게시글 존재 확인.
2. `ArticleLikeRepository`에서 (유저-게시글) 쌍 조회.
3. 존재 시 삭제 (`liked=false`), 미존재 시 생성 (`liked=true`).
4. 변경된 좋아요 수와 상태 반환.
