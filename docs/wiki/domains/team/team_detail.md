# 팀 상세 조회 (Team Detail)

## 📡 API Specification
**`GET /v1/teams/{teamId}`**

*   **Description**: 팀의 상세 정보(기술 스택, 멤버 목록 등)를 조회합니다.
*   **Permission Name**: `team:read`
*   **Permissions**: `ANONYMOUS` (or `USER`)

### Response
*   **200 OK**
```json
{
  "id": 1,
  "name": "KOSP 개발팀",
  "description": "오픈소스 플랫폼 개발 프로젝트",
  "techStacks": ["Spring", "React"],
  "leader": { "id": 10, "nickname": "홍길동" },
  "members": [
    { "id": 11, "nickname": "김철수", "role": "MEMBER" }
  ],
  "createdAt": "2024-12-01T09:00:00"
}
```

*   **404 Not Found**
```json
{
  "code": "TEAM_NOT_FOUND",
  "message": "팀을 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `TeamController.getTeam`
*   **Flow**:
1. `TeamRepository`에서 팀 조회 (Fetch Join으로 멤버, 기술스택 로드 권장).
2. `TeamDetailResponse` DTO 매핑 및 반환.
