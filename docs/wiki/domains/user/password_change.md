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
  "code": "PASSWORD_MISMATCH",
  "message": "현재 비밀번호가 일치하지 않습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `UserController.updatePassword`
*   **Flow**:
1. `currentPassword`와 DB 저장된 해시값(`BCrypt`) 비교.
2. `newPassword` 형식(정규식) 검증.
3. 새 비밀번호 해싱 후 `User` 엔티티 업데이트.
