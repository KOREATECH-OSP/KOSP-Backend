# 소셜 로그인 (Social Login)

## 📡 API Specification
**`GET /login/oauth2/code/{provider}`**

*   **Description**: GitHub 등 OAuth Provider로부터 받은 코드로 로그인을 수행합니다.
*   **Provider**: `github`

### Request (Query Param)
*   `code`: OAuth Authorization Code

### Response
*   **302 Found**: 프론트엔드 URL로 리다이렉트 (로그인 성공 시 Session Cookie 포함)

---

## 🛠️ Implementation Details
*   **Filter**: `OAuth2LoginAuthenticationFilter`
*   **Handler**: `CustomOAuth2UserService`
*   **Logic**:
    *   `loadUser()`: GitHub API로 사용자 정보 조회.
    *   DB에 `githubId`로 존재하는지 확인.
    *   **신규 유저**: `User` 엔티티 생성 (Role=TEMP or GUEST), DB 저장 후 로그인.
    *   **기존 유저**: 정보(프로필 사진 등) 업데이트 후 로그인.
