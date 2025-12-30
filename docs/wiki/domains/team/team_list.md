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
  "content": [
    {
      "id": 1,
      "name": "KOSP 개발팀",
      "memberCount": 4,
      "leader": "홍길동",
      "imageUrl": "..."
    }
  ],
  "pageable": { ... },
  "totalElements": 5
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `TeamController.getList`
*   **Flow**:
1. `QueryDSL`을 사용하여 팀 목록 조회.
2. `memberCount`는 서브쿼리나 배치 조회로 최적화.
