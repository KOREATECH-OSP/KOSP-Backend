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
  "message": "해당 이메일로 가입된 사용자가 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AuthController.sendPasswordResetMail`
*   **Flow**:
1. 이메일로 사용자 존재 여부 확인.
2. 존재 시 재설정 토큰(JWT or Random String) 생성.
3. 이메일 템플릿에 토큰을 포함한 링크(`FRONTEND_URL/reset-password?token=...`)를 담아 발송.
