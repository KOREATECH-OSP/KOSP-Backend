# 사용자 도메인 (User)

## <a id="signup"></a> 1. 회원가입 (Signup)
OAuth2 로그인 또는 이메일 인증 후, 최종적으로 회원을 등록하는 절차입니다.

### 🛠️ 구현 상세 (Implementation)
*   **선행 조건**: 이메일 인증이 완료(`isVerified=true`)되어야 함.
*   **로직**:
    1.  `UserService.signup()` 호출.
    2.  `EmailVerificationService`를 통해 이메일 인증 여부 확인 (미인증 시 예외 발생).
    3.  인증 데이터 삭제 (재사용 방지).
    4.  비밀번호 `BCrypt` 해싱 후 DB 저장.
    5.  기본 권한(`ROLE_STUDENT`) 부여.

### 📡 API 명세 (Specification)
*   **Endpoint**: `POST /v1/users/signup`
*   **Request**:
    ```json
    {
      "name": "박성빈",
      "kutId": "2020136xxx",
      "kutEmail": "kosp@koreatech.ac.kr",
      "password": "password123!",
      "githubId": 12345
    }
    ```

---

## <a id="password-reset"></a> 2. 비밀번호 재설정 (Password Reset)
비밀번호를 분실했을 때 이메일 인증을 통해 재설정합니다.

### 🛠️ 구현 상세 (Implementation)
*   **Flow**:
    1.  **발송 요청**: `UserPasswordService`가 30분 유효한 토큰 생성 및 이메일 발송.
        *   이때 `@ServerURL`을 사용하여 로컬/운영 환경에 맞는 링크 생성.
    2.  **재설정**: 사용자가 링크(토큰 포함)를 통해 새 비밀번호 전송.
    3.  **검증**: 토큰 유효성 및 만료 여부 검증 후 비밀번호 변경.

### 📡 API 명세 (Specification)
*   **발송**: `POST /v1/auth/password/reset` (Body: `{ "email": "..." }`)
*   **확정**: `POST /v1/auth/password/reset/confirm`
    *   Body: `{ "token": "uuid...", "newPassword": "..." }`

---

## <a id="update-profile"></a> 3. 회원 정보 수정 (Update Profile)
본인의 프로필 정보를 수정합니다.

### 🛠️ 구현 상세 (Implementation)
*   **보안**: 본인만 수정 가능 (`/v1/users/me` 엔드포인트 사용).
*   **로직**: 변경 감지(Dirty Checking)를 통해 엔티티 업데이트.

### 📡 API 명세 (Specification)
*   **Endpoint**: `PUT /v1/users/me`
