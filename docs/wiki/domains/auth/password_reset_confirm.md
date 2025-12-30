# 비밀번호 재설정 확인 (Password Reset Confirm)

## 📡 API Specification
**`POST /v1/auth/password/reset/confirm`**

*   **Description**: 발급된 토큰을 검증하고 새로운 비밀번호로 변경합니다.
*   **Permission Name**: `auth:password:reset-confirm`
*   **Permissions**: `ANONYMOUS`

### Request
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "newPassword": "newPassword123!"
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
  "message": "비밀번호 형식이 올바르지 않습니다."
}
```

*   **404 Not Found**
```json
{
  "code": "TOKEN_NOT_FOUND",
  "message": "유효하지 않거나 만료된 토큰입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AuthController.resetPassword`
*   **Service**: `UserPasswordService.resetPassword`
*   **Flow**:
1. Request Body로 `token`과 `newPassword` 수신.
2. Redis에서 토큰 조회 (없을 시 404).
3. 해당 토큰에 매핑된 `userId`로 유저 조회.
4. 비밀번호 암호화 및 변경.
5. Redis 토큰 삭제.
