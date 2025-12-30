# 챌린지 생성 (Admin Challenge Create)

## 📡 API Specification
**`POST /v1/admin/challenges`**

*   **Description**: 관리자 권한으로 새로운 챌린지를 생성합니다.
*   **Permission Name**: `admin:challenge:create`
*   **Permissions**: `ADMIN`

### Request
```json
{
  "name": "commits-100",
  "description": "총 커밋 100개 달성",
  "tier": 1,
  "condition": "user.totalCommits >= 100", // SpEL Expression
  "imageUrl": "..."
}
```

### Response
*   **201 Created**
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

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.createChallenge`
*   **Service**: `ChallengeService.createChallenge`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. `SpelExpressionParser`를 사용하여 `condition` 문자열 파싱 검증.
3. 파싱 실패 시 `INVALID_CHALLENGE_CONDITION` 예외 발생.
4. `Challenge` 엔티티 생성 및 DB 저장.
