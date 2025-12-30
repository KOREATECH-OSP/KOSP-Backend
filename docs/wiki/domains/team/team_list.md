# 팀 목록 조회 (Team List)

## 📡 API Specification
**`GET /v1/teams`**

*   **Description**: 현재 생성된 팀 목록을 조회합니다. 검색 및 페이징을 지원합니다.
*   **Permission Name**: `team:list`
*   **Permissions**: `ANONYMOUS` (or `USER`)

### Request (Query Parameters)
*   `search`: `String` (Optional, 팀 명/설명 검색)
*   `page`: `Integer` (Default: 0)
*   `size`: `Integer` (Default: 10)

### Response
*   **200 OK**
```json
{
  "teams": [
    {
      "id": 1,
      "name": "KOSP 개발팀",
      "memberCount": 4,
      "imageUrl": "..."
    }
  ],
  "meta": {
    "page": 1,
    "size": 10,
    "totalCount": 5,
    "totalPages": 1
  }
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `TeamController.getList`
*   **Service**: `TeamService.getList`
*   **Flow**:
1. `TeamRepository` 조회 (검색어 `search` 포함).
2. `getLeaderName()`: 각 팀의 리더 이름 추출 (Stream Filter).
3. `TeamListResponse` (목록 + Meta) 반환.
