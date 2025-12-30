# 챌린지 삭제 (Admin Challenge Delete)

## 📡 API Specification
**`DELETE /v1/admin/challenges/{challengeId}`**

*   **Description**: 등록된 챌린지를 삭제합니다.
*   **Permission Name**: `admin:challenges:delete`
*   **Permissions**: `ADMIN`

### Response
*   **204 No Content**
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
*   **Controller**: `AdminController.deleteChallenge`
*   **Service**: `ChallengeService.deleteChallenge`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. `ChallengeRepository`에서 챌린지 조회 (없을 시 예외).
3. 챌린지 삭제 (Hard Delete or Soft Delete - 현재 코드상 `repository.delete()`).
