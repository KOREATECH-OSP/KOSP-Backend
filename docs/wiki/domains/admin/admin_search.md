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

*   **401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다."
}
```

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "접근 권한이 없습니다 (관리자 권한 필요)."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.search`
*   **Service**: `AdminSearchService.search`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. `keyword` 유효성 검사 (Null check -> Empty List 반환).
3. `type` 파라미터(`USER`, `ARTICLE`, `ALL`)에 따라 분기 처리.
    *   `USER`: `UserRepository.findByNameContaining`
    *   `ARTICLE`: `ArticleRepository.findByTitleContaining`
4. 검색 결과를 `AdminSearchResponse`로 래핑하여 반환.
