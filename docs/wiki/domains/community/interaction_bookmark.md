# 게시글 북마크 (Article Bookmark)

## 📡 API Specification
**`POST /v1/community/articles/{id}/bookmarks`**

*   **Description**: 게시글을 내 보관함에 저장하거나 취소합니다.
*   **Permission Name**: `community:article:bookmark`
*   **Permissions**: `USER`

### Response
*   **200 OK**
```json
{
  "isBookmarked": true
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
*   **Controller**: `ArticleController.toggleBookmark`
*   **Flow**:
1. `ArticleRepository` 게시글 확인.
2. `BookmarkRepository` (User-Article) 조회.
3. 존재 시 삭제, 미존재 시 생성.
