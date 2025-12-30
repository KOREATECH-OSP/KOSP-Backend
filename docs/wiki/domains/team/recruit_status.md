# 모집 상태 변경 (Recruit Status)

## 📡 API Specification
**`PATCH /v1/community/recruits/{id}/status`**

*   **Description**: 모집 공고의 상태(OPEN/CLOSED)를 변경합니다.
*   **Permission Name**: `recruit:status`
*   **Permissions**: `USER` (팀 리더)

### Request
```json
{
  "status": "CLOSED"
}
```

### Response
*   **200 OK**
```json
// No Content
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.updateStatus`
*   **Flow**:
1. Path ID로 공고 조회.
2. 상태값 변경 (`OPEN` <-> `CLOSED`).
