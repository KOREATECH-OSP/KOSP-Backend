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
  "liked": true,
  "count": 16
}
```

*   **401 Unauthorized** (비로그인)
```json
{
  "code": "UNAUTHORIZED",
  "message": "로그인이 필요합니다."
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
