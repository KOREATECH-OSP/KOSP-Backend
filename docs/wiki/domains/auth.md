# 인증 도메인 (Authentication)

## <a id="email-login"></a> 1. 이메일 로그인 (Email Login)
기존 회원(자체 가입)을 위한 로그인 프로세스입니다.

### 🛠️ 구현 상세 (Implementation)
*   **관련 클래스**: `AuthController`, `AuthService`, `CustomUserDetailsService`
*   **로직**:
    1.  사용자 이메일 기반으로 DB(`UserRepository`)에서 `User` 조회.
    2.  `BCryptPasswordEncoder`로 비밀번호 일치 여부 확인.
    3.  인증 성공 시, Spring Security `SecurityContext` 생성 및 세션 저장 (Redis).

### 📡 API 명세 (Specification)
*   **Endpoint**: `POST /v1/auth/login`
*   **Request**:
    ```json
    {
      "email": "user@koreatech.ac.kr",
      "password": "rawPassword123!"
    }
    ```
*   **Response**: `200 OK` (Set-Cookie: JSESSIONID)

---

## <a id="social-login"></a> 2. 소셜 로그인 (Social Login)
GitHub OAuth2를 이용한 로그인 프로세스입니다.

### 🛠️ 구현 상세 (Implementation)
*   **관련 클래스**: `CustomOAuth2UserService`
*   **로직**:
    1.  Spring Security OAuth2 Client가 GitHub 리소스 서버로부터 사용자 정보 획득.
    2.  `CustomOAuth2UserService`에서 DB 조회:
        *   신규 유저: `User` 엔티티 생성 (GUEST 권한).
        *   기존 유저: 정보 업데이트.
    3.  세션 생성 및 로그인 처리.

### 📡 API 명세 (Specification)
*   **Endpoint**: `GET /oauth2/authorization/github`
*   **Response**: 리다이렉트 (GitHub 로그인 페이지)

---

## <a id="get-me"></a> 3. 내 정보 조회 (Get Me)
현재 로그인한 사용자의 세션 정보를 기반으로 프로필을 조회합니다.

### 🛠️ 구현 상세 (Implementation)
*   **보안**: URL에 `userId`를 노출하지 않음 (`/v1/users/{id}` 대신 `/me` 사용).
*   **로직**: `SecurityContextHolder`에서 Authentication 객체를 꺼내어 사용자 식별.

### 📡 API 명세 (Specification)
*   **Endpoint**: `GET /v1/auth/me`
*   **Response**:
    ```json
    {
      "id": 1,
      "email": "user@koreatech.ac.kr",
      "name": "홍길동",
      "profileImage": "..."
    }
    ```

---

## <a id="logout"></a> 4. 로그아웃 (Logout)
서버 세션을 만료시키고 Redis에서 데이터를 삭제합니다.

### 📡 API 명세 (Specification)
*   **Endpoint**: `POST /v1/auth/logout`
*   **Response**: `200 OK` (Set-Cookie: JSESSIONID=; Max-Age=0)
