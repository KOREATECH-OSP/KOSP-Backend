# 회원가입 (Signup)

## 📡 API Specification
**`POST /v1/users/signup`**

*   **Description**: 학교 이메일 인증이 완료된 후, 최종적으로 사용자 정보를 입력하여 가입합니다.
*   **Permission Name**: `user:signup`
*   **Permissions**: `ANONYMOUS`

### Request
```json
{
  "email": "kosp@koreatech.ac.kr",
  "password": "password123!",
  "name": "홍길동",
  "nickname": "spartacoding",
  "studentId": "2020136xxx"
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
  "code": "EMAIL_NOT_VERIFIED",
  "message": "이메일 인증이 완료되지 않았습니다."
}
```
*   **409 Conflict**
```json
{
  "code": "DUPLICATE_USER",
  "message": "이미 존재하는 이메일 또는 학번입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `UserController.signup`
*   **Flow**:
1. `EmailVerificationService`에서 이메일 인증 완료(`verified` 상태) 여부 확인.
2. `UserRepository`에서 이메일/학번 중복 검사.
3. 비밀번호 해싱 (`BCrypt`) 및 `User` 엔티티 생성.
4. 사용자 저장 및 기본 권한(`USER`) 부여.
5. (Optional) 자동 로그인 처리.
