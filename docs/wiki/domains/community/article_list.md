# 게시글 목록 조회 (Article List)

## 📡 API Specification
**`GET /v1/community/articles`**

*   **Description**: 게시판별 게시글 목록을 조회합니다. 검색 및 페이징을 지원합니다.
*   **Permission Name**: `community:article:list`
*   **Permissions**: `ANONYMOUS` (or `USER` depending on board policy)

### Request (Query Parameters)
*   `boardId`: `Long` (Required)
*   `page`: `Integer` (Default: 0)
*   `size`: `Integer` (Default: 10)
*   `keyword`: `String` (Optional)

### Response
*   **200 OK**
```json
{
  "posts": [
    {
      "id": 1,
      "title": "KOSP 프로젝트 소개",
      "author": { "nickname": "관리자" },
      "views": 120,
      "likes": 15,
      "comments": 3,
      "createdAt": "2024-12-01T10:00:00"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "totalCount": 25,
    "totalPages": 3
  }
}
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "요청 파라미터가 올바르지 않습니다."
}
```

*   **404 Not Found**
```json
{
  "code": "BOARD_NOT_FOUND",
  "message": "게시판을 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `ArticleController.getList`
*   **Service**: `ArticleService.getList`
*   **Flow**:
1. `BoardService`를 통해 `boardId`로 게시판 조회 (없을 시 404).
2. `ArticleRepository`에서 해당 게시판의 글 목록 조회 (Pagination 적용).
3. 각 게시글에 대해 로그인한 사용자의 `isLiked`, `isBookmarked` 여부 확인 후 응답 구성.
