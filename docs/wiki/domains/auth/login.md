# 이메일 로그인 (Email Login)

## 📡 API Specification
**`POST /v1/auth/login`**

*   **Description**: 자체 회원가입한 사용자의 이메일과 비밀번호(SHA-256 Hashed)로 로그인하여 세션을 생성합니다.
*   **Permission Name**: `auth:login`
*   **Permissions**: `ANONYMOUS`

### Request
```json
{
  "email": "user@koreatech.ac.kr",
  "password": "cd06f8c2b0dd065faf6ef910c7f1234567890abcdef1234567890abcdef12345" // SHA-256 Hashed
}
```

### Response
*   **200 OK**
    *   Headers: `Set-Cookie: JSESSIONID=...; Path=/; HttpOnly; SameSite=Strict`
```json
// No Content (Cookie Only)
```

*   **400 Bad Request** (입력값 오류)
```json
{
  "code": "VALIDATION_ERROR",
  "message": "이메일 형식에 맞지 않습니다.",
  "errors": [...]
}
```

*   **401 Unauthorized** (인증 실패)
```json
{
  "code": "AUTH_FAILED",
  "message": "이메일 또는 비밀번호가 일치하지 않습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AuthController.login`
*   **Service**: `AuthService.authenticate`
*   **Flow**:
1. `UserRepository`에서 이메일로 사용자 조회.
2. `BCryptPasswordEncoder.matches()`로 비밀번호 검증.
3. 검증 성공 시 `UserPrincipal` 생성 -> `SecurityContextHolder`에 저장.
4. Redis에 세션 정보 저장 (`spring-session-data-redis`).
