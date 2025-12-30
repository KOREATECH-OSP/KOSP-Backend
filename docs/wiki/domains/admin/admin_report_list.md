# 신고 접수 목록 조회 (Admin Report List)

## 📡 API Specification
**`GET /v1/admin/reports`**

*   **Description**: 접수된 모든 신고 목록을 조회합니다.
*   **Permission Name**: `admin:report:list`
*   **Permissions**: `ADMIN`

### Response
*   **200 OK**
```json
{
  "content": [
    {
      "id": 1,
      "targetType": "ARTICLE",
      "targetId": 100,
      "reason": "SPAM",
      "reporter": { "id": 10, "nickname": "Reporter" },
      "status": "PENDING",
      "createdAt": "2024-12-30T15:00:00"
    }
  ],
  "pageable": { ... },
  "totalElements": 5
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.getAllReports`
*   **Flow**:
1. `ReportRepository` 전체 조회 (상태별 필터링 기능 추가 가능).
