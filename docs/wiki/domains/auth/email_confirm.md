# 이메일 인증 확인 (Email Confirm)

## 📡 API Specification
**`POST /v1/auth/email/verify/confirm`**

*   **Description**: 수신한 인증코드를 검증하여 이메일 인증을 완료합니다.
*   **Permission Name**: `auth:email:confirm`
*   **Permissions**: `ANONYMOUS`

### Request
```json
{
  "email": "kosp@koreatech.ac.kr",
  "code": "123456"
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
  "code": "INVALID_CODE",
  "message": "인증코드가 일치하지 않거나 만료되었습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AuthController.verifyCode`
*   **Flow**:
1. Redis에서 `email:auth:{email}` 값 조회.
2. 요청된 `code`와 Redis 값 비교.
3. 일치 시 Redis 인증 데이터 삭제 및 `email:verified:{email}` 키 생성 (TTL 30분).
4. 이후 회원가입 요청 시 `email:verified:{email}` 존재 여부 확인.
