# 사용자 통합 검색 (Admin Search)

## 📡 API Specification
**`GET /v1/admin/search`**

*   **Description**: 관리자 페이지에서 사용자 및 콘텐츠를 검색합니다. (사용자 목록 조회 기능 포함)
*   **Permission Name**: `admin:search`
*   **Permissions**: `ADMIN`

### Request (Query Parameters)
*   `keyword`: `String` (Required for search, Optional for list)
*   `type`: `USER` | `ARTICLE` | `ALL` (Default: `ALL`)
*   `page`: `Integer` (Default: 0)

### Response
*   **200 OK**
```json
{
  "users": [
    { "id": 1, "email": "...", "nickname": "...", "role": "USER" }
  ],
  "articles": [ ... ]
}
```

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "접근 권한이 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.search`
*   **Flow**:
1. 권한 검사 (`Role=ADMIN`).
2. `AdminSearchService`에서 타입별 검색 쿼리 실행.
