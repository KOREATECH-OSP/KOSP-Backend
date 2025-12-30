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
  "imageUrl": "https://...",
  "members": [
    { "id": 11, "nickname": "김철수", "role": "MEMBER" }
  ]
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
*   **Service**: `TeamService.getTeam`
*   **Flow**:
1. `TeamRepository.getById(id)` 호출.
2. 존재하지 않을 경우 `TEAM_NOT_FOUND` 예외 발생 (404).
3. `TeamDetailResponse` 변환 및 반환.(Fetch Join으로 멤버, 기술스택 로드 권장).
