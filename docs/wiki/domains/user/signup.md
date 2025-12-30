# 회원가입 (Signup)

## 📡 API Specification
**`POST /v1/users/signup`**

*   **Description**: 학교 이메일 인증이 완료된 후, 사용자 정보를 입력하여 가입합니다.
*   **Permission Name**: `user:signup`
*   **Permissions**: `ANONYMOUS`

### Request
```json
{
  "kutEmail": "kosp@koreatech.ac.kr",
  "password": "password123!", // Plaintext (8자 이상, 영문/숫자/특수문자 조합)
  "name": "박성빈",
  "kutId": "2023100514",
  "githubId": 12345678
}
```

### Response
*   **201 Created**
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "이메일 형식이 올바르지 않습니다."
  "message": "비밀번호 형식이 올바르지 않습니다."
}
```

*   **409 Conflict**
```json
{
  "code": "USER_ALREADY_EXISTS",
  "message": "이미 가입된 이메일입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `UserController.signup`
*   **Service**: `UserService.signup`
*   **DTO**: `UserSignupRequest`
*   **Flow**:
1. 이메일 인증 완료 여부 확인 (`EmailVerificationService`).
2. `GithubUser` (소셜 계정) 연동 정보 조회.
3. 이메일 중복 확인 (탈퇴한 회원이면 복구 Process).
4. `User` 엔티티 생성, 비밀번호 인코딩, DB 저장.
5. 자동 로그인 처리 (`AuthService.login`).
