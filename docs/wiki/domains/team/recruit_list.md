# 모집 공고 목록 조회 (Recruit List)

## 📡 API Specification
**`GET /v1/community/recruits`**

*   **Description**: 현재 모집 중인 공고들을 조회합니다. 페이징 및 상태 필터링을 지원합니다.
*   **Permission Name**: `recruit:list`
*   **Permissions**: `ANONYMOUS` (or `USER`)

### Request (Query Parameters)
*   `page`: `Integer` (Default: 0)
*   `size`: `Integer` (Default: 10)
*   `status`: `OPEN` (모집중) / `CLOSED` (마감) / `ALL` (전체)
*   `teamId`: `Long` (Optional, 특정 팀의 공고만 조회)

### Response
*   **200 OK**
```json
{
  "content": [
    {
      "id": 5,
      "team": { "name": "KOSP팀" },
      "title": "백엔드 개발자 구인",
      "status": "OPEN",
      "deadline": "2025-01-31T23:59:59"
    }
  ],
  "pageable": { ... },
  "totalElements": 20
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.getList`
*   **Flow**:
1. QueryDSL을 사용하여 조건에 맞는 공고 조회.
2. 마감된 공고는 필터링 조건에 따라 포함 여부 결정.
