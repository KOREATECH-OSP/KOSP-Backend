# 모집 공고 작성 (Recruit Create)

## 📡 API Specification
**`POST /v1/community/recruits`**

*   **Description**: 생성된 팀과 연동하여 팀원 모집 공고를 작성합니다.
*   **Permission Name**: `recruit:create`
*   **Permissions**: `USER`

### Request
```json
{
  "teamId": 1,
  "title": "[모집] 백엔드 개발자 구합니다",
  "content": "API 설계 및 구현 담당...",
  "deadline": "2025-01-31T23:59:59"
}
```

### Response
*   **201 Created**
    *   Headers: `Location: /v1/community/recruits/{id}`
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "마감일은 현재 시간보다 미래여야 합니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.create`
*   **Flow**:
1. `teamId` 유효성 검증 (존재 여부 및 작성자가 팀 리더인지 확인).
2. `Recruit` 엔티티 생성.
3. `Recruit` 저장 및 201 응답.
