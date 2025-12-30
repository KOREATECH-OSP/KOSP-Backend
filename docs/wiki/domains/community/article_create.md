# 게시글 작성 (Article Create)

## 📡 API Specification
**`POST /v1/community/articles`**

*   **Description**: 지정된 게시판(`boardId`)에 새로운 글을 작성합니다.
*   **Permission Name**: `community:article:create`
*   **Permissions**: `USER`

### Request
```json
{
  "boardId": 1,
  "title": "Spring Boot 질문있습니다",
  "content": "JPA N+1 문제는 어떻게 해결하나요?",
  "tags": ["Spring", "JPA"],
  "images": ["url1", "url2"]
}
```

### Response
*   **201 Created**
```json
// No Content (Location Header Included)
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "제목은 필수입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `ArticleController.create`
*   **Flow**:
1. `BoardService`를 통해 `boardId` 유효성 검증.
2. `Article` 엔티티 생성 및 저장.
3. 생성된 게시글 ID를 `Location` 헤더에 담아 201 응답 반환.
