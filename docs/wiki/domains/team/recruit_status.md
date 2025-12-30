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
  "message": "권한이 없습니다 (본인 작성 공고만 상태 변경 가능)."
}
```

*   **404 Not Found**
```json
{
  "code": "RECRUIT_NOT_FOUND",
  "message": "모집 공고를 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.updateStatus`
*   **Service**: `RecruitService.updateStatus`
*   **Flow**:
1. `RecruitRepository`에서 ID로 공고 조회 (없을 시 404).
2. `validateOwner()`: 작성자 본인 확인 (아닐 경우 403).
3. 상태 변경 (`RecruitStatus`: OPEN -> CLOSED 등).
