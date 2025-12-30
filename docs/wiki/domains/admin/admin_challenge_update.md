# 챌린지 수정 (Admin Challenge Update)

## 📡 API Specification
**`PUT /v1/admin/challenges/{challengeId}`**

*   **Description**: 등록된 챌린지 정보를 수정합니다.
*   **Permission Name**: `admin:challenges:update`
*   **Permissions**: `ADMIN`

### Request
```json
{
  "name": "commits-200",
  "description": "총 커밋 200개 달성",
  "tier": 2,
  "condition": "user.totalCommits >= 200",
  "imageUrl": "..."
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
  "code": "INVALID_CHALLENGE_CONDITION",
  "message": "챌린지 조건식(SpEL) 형식이 올바르지 않습니다."
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
  "message": "접근 권한이 없습니다 (관리자 권한 필요)."
}
```

*   **404 Not Found**
```json
{
  "code": "CHALLENGE_NOT_FOUND",
  "message": "챌린지를 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.updateChallenge`
*   **Service**: `ChallengeService.updateChallenge`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. `ChallengeRepository`에서 챌린지 조회.
3. 조건식(`condition`)이 변경된 경우 SpEL 문법 재검증.
4. 챌린지 정보 업데이트.
