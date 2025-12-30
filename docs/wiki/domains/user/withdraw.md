# 회원 탈퇴 (Withdrawal)

## 📡 API Specification
**`DELETE /v1/users/{userId}`**

*   **Description**: 회원을 탈퇴 처리합니다. (데이터 Soft Delete 적용)
*   **Permission Name**: `user:withdraw`
*   **Permissions**: `USER` (본인) or `ADMIN`

### Response
*   **204 No Content**
```json
// No Content
```

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "권한이 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `UserController.delete`
*   **Flow**:
1. PathVariable `userId` 검증.
2. `User` 엔티티의 `deleted` 필드를 `true`로 설정 (Soft Delete).
3. 연관된 토큰/세션 만료 처리.
