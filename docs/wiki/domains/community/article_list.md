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
  "content": [
    {
      "id": 10,
      "title": "Spring Boot 질문있습니다",
      "author": { "id": 1, "nickname": "홍길동" },
      "views": 15,
      "likes": 3,
      "createdAt": "2024-12-30T10:00:00"
    }
  ],
  "pageable": { ... },
  "totalElements": 100,
  "totalPages": 10
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `ArticleController.getList`
*   **Flow**:
1. `BoardService`에서 `boardId` 확인.
2. `QueryDSL`을 사용하여 조건(Board, Keyword)에 맞는 게시글 페이징 조회.
