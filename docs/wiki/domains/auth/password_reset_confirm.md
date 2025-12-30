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
  "code": "INVALID_TOKEN",
  "message": "유효하지 않거나 만료된 토큰입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AuthController.resetPassword`
*   **Flow**:
1. 토큰 유효성 검증 (서명 및 만료 시간 확인).
2. 토큰 내 사용자 정보(Email/ID) 추출.
3. 해당 사용자의 비밀번호를 새 비밀번호(Hash)로 업데이트.
4. (선택) 기존 세션 만료 처리.
