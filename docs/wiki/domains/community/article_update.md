# 게시글 수정 (Article Update)

## 📡 API Specification
**`PUT /v1/community/articles/{id}`**

*   **Description**: 본인이 작성한 게시글을 수정합니다.
*   **Permission Name**: `community:article:update`
*   **Permissions**: `USER` (본인)

### Request
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "tags": ["Spring", "JPA", "Edit"],
  "images": []
}
```

### Response
*   **200 OK**
```json
// No Content
```

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "본인의 게시글만 수정할 수 있습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `ArticleController.update`
*   **Flow**:
1. Path ID로 게시글 조회.
2. 작성자와 현재 로그인 유저 일치 여부 확인.
3. 제목, 내용, 태그 등 업데이트.
