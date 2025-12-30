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
  "title": "KOSP 프로젝트 소개",
  "content": "KOSP는 오픈소스 기여 증명을 위한 플랫폼입니다.",
  "tags": ["OpenSource", "Project"]
}
```

### Response
*   **201 Created**
    *   Headers: `Location: /v1/community/articles/{id}`
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "제목은 필수입니다."
}
```

*   **401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다."
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
*   **Controller**: `ArticleController.create`
*   **Service**: `ArticleService.create`
*   **Flow**:
1. `BoardService`를 통해 `boardId` 유효성 검증 (존재하지 않으면 404 에러).
2. `Article` 엔티티 생성 (제목, 내용, 태그 포함).
3. `ArticleRepository.save()` 호출.
4. 생성된 게시글 ID를 `Location` 헤더에 담아 201 응답 반환.
