# 비밀번호 재설정 요청 (Password Reset Request)

## 📡 API Specification
**`POST /v1/auth/password/reset`**

*   **Description**: 비밀번호를 잊은 경우, 재설정 링크(또는 토큰)를 이메일로 발송합니다.
*   **Permission Name**: `auth:password:reset-request`
*   **Permissions**: `ANONYMOUS`

### Request
```json
{
  "email": "user@koreatech.ac.kr"
}
```

### Response
*   **200 OK**
```json
// No Content
```

*   **404 Not Found**
```json
{
  "code": "USER_NOT_FOUND",
  "message": "가입되지 않은 이메일입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AuthController.sendPasswordResetMail`
*   **Service**: `UserPasswordService.sendPasswordResetMail`
*   **Flow**:
1. `UserRepository`에서 이메일로 사용자 조회 (없을 시 404).
2. 비밀번호 재설정 토큰 생성 (UUID).
3. Redis에 토큰 저장 (`password:reset:{token}`, TTL 30분).
4. 재설정 링크가 포함된 메일 발송 (`ServerURL` + `/reset-password?token={token}`).
