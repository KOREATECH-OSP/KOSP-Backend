# 로그아웃 (Logout)

## 📡 API Specification
**`POST /v1/auth/logout`**

*   **Description**: 현재 사용자의 세션을 만료시킵니다 (Server-side & Client-side).
*   **Permission Name**: `auth:logout`
*   **Permissions**: `USER`

### Response
*   **200 OK**
    *   Headers: `Set-Cookie: JSESSIONID=; Path=/; Max-Age=0`
```json
// No Content
```

*   **401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AuthController.logout`
*   **Flow**:
1. `SecurityConfig`의 `logout().logoutUrl("/v1/auth/logout")` 필터 체인 동작.
2. Redis에서 해당 Session Key 삭제.
3. `JSESSIONID` 쿠키 무효화 응답 전송.
