# 모집 공고 수정 (Recruit Update)

## 📡 API Specification
**`PUT /v1/community/recruits/{id}`**

*   **Description**: 본인이 작성한(팀 리더) 모집 공고를 수정합니다.
*   **Permission Name**: `recruit:update`
*   **Permissions**: `USER` (팀 리더)

### Request
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "deadline": "2025-02-15T23:59:59"
}
```

### Response
*   **200 OK**
```json
// No Content
```

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "작성자(팀 리더)만 수정할 수 있습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.update`
*   **Flow**:
1. Path ID로 공고 조회.
2. 현재 유저가 해당 공고의 팀 리더인지 검증.
3. 제목, 내용, 마감일 수정.
