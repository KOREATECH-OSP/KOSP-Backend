# 이메일 인증코드 발송 (Email Send)

## 📡 API Specification
**`POST /v1/auth/email/verify`**

*   **Description**: 재학생 인증을 위해 `@koreatech.ac.kr` 메일로 코드를 발송합니다.
*   **Permission Name**: `auth:email:send`
*   **Permissions**: `ANONYMOUS`

### Request
```json
{
  "email": "kosp@koreatech.ac.kr"
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
  "code": "INVALID_EMAIL_DOMAIN",
  "message": "코리아텍 이메일(@koreatech.ac.kr)만 사용 가능합니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AuthController.sendCertificationMail`
*   **Flow**:
1. 이메일 도메인 유효성 검사.
2. 6자리 랜덤 인증코드 생성.
3. Redis에 `email:auth:{email}` 키로 저장 (TTL 5분).
4. AWS SES (`SesMailSender`)를 통해 메일 발송.
