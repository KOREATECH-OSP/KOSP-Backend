# 비밀번호 변경 (Change Password)

## 📡 API Specification
**`PUT /v1/users/me/password`**

*   **Description**: 로그인된 상태에서 본인의 비밀번호를 변경합니다.
*   **Permission Name**: `user:password:change`
*   **Permissions**: `USER`

### Request
```json
{
  "currentPassword": "oldPassword123!",
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
  "message": "새 비밀번호 형식이 올바르지 않습니다."
}
```

*   **401 Unauthorized**
```json
{
  "code": "AUTHENTICATION_FAILED",
  "message": "현재 비밀번호가 일치하지 않습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `UserController.updatePassword`
*   **Service**: `UserService.changePassword`
*   **Flow**:
1. 현재 비밀번호 검증 (`PasswordEncoder.matches`). 불일치 시 예외.
2. 새 비밀번호 유효성 검증.
3. 비밀번호 인코딩 후 저장.
