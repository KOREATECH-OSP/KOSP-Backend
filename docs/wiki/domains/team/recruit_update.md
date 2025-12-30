# 모집 공고 수정 (Recruit Update)

## 📡 API Specification
**`PUT /v1/community/recruits/{id}`**

*   **Description**: 본인이 작성한(팀 리더) 모집 공고를 수정합니다.
*   **Permission Name**: `recruit:update`
*   **Permissions**: `USER` (팀 리더)

### Request
```json
{
  "boardId": 3,
  "teamId": 1,
  "title": "수정된 제목",
  "content": "수정된 내용",
  "tags": ["Spring"],
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-02-15T23:59:59"
}
```

### Response
*   **200 OK**
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "제목은 필수입니다."
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
  "message": "권한이 없습니다 (본인 작성 공고만 수정 가능)."
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
*   **Controller**: `RecruitController.update`
*   **Service**: `RecruitService.update`
*   **Flow**:
1. `RecruitRepository`에서 ID로 공고 조회 (없을 시 404).
2. `validateOwner()`: 작성자 본인 확인 (아닐 경우 403).
3. `Recruit` 정보 업데이트 (제목, 내용, 태그, 팀, 기간 등).
