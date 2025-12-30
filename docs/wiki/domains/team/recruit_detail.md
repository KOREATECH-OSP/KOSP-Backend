# 모집 공고 상세 조회 (Recruit Detail)

## 📡 API Specification
**`GET /v1/community/recruits/{id}`**

*   **Description**: 모집 공고의 상세 내용을 조회합니다.
*   **Permission Name**: `recruit:read`
*   **Permissions**: `ANONYMOUS` (or `USER`)

### Response
*   **200 OK**
```json
{
  "id": 5,
  "team": { "name": "KOSP팀", "leader": "홍길동" },
  "title": "백엔드 개발자 구인",
  "content": "상세 내용...",
  "status": "OPEN",
  "deadline": "2025-01-31T23:59:59",
  "createdAt": "2025-01-01T10:00:00"
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
*   **Controller**: `RecruitController.getOne`
*   **Flow**:
1. `RecruitRepository`에서 ID로 조회.
2. `RecruitResponse` DTO 변환 및 반환.
