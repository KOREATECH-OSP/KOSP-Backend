# 팀 생성 (Team Create)

## 📡 API Specification
**`POST /v1/teams`**

*   **Description**: 프로젝트/스터디를 위한 팀 공간을 생성합니다.
*   **Permission Name**: `team:create`
*   **Permissions**: `USER`

### Request
```json
{
  "name": "KOSP 개발팀",
  "description": "오픈소스 플랫폼 개발 프로젝트",
  "imageUrl": "https://..."
}
```

### Response
*   **201 Created**
    *   Headers: `Location: /v1/teams/{id}`
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "팀 이름은 필수입니다."
}
```

*   **401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `TeamController.create`
*   **Flow**:
1. 팀 이름 중복 검사 (Optional).
2. `Team` 엔티티 생성.
3. 생성자(`User`)를 `TeamMember` (Role=LEADER)로 추가 (One Transaction).
4. `Location` 헤더 포함 응답.
