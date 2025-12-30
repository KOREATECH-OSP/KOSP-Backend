# 프로필 수정 (Update Profile)

## 📡 API Specification
**`PUT /v1/users/{userId}`**

*   **Description**: 본인의 프로필 정보(닉네임, 소개글 등)를 수정합니다.
*   **Permission Name**: `user:profile:update`
*   **Permissions**: `USER` (본인)

### Request
```json
{
    "nickname": "new_nickname",
    "introduction": "Hello World"
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
  "code": "VALIDATION_ERROR",
  "message": "이름은 필수입니다."
}
```

*   **401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다."
}
```

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "권한이 없습니다 (본인 정보만 수정 가능)."
}
```

*   **404 Not Found**
```json
{
  "code": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `UserController.update`
*   **Service**: `UserService.update`
*   **Flow**:
1. 요청한 유저(`AuthUser`)와 대상 유저 ID 일치 여부 확인 (Controller Level).
2. 불일치 시 `FORBIDDEN` 예외 발생.
3. `UserRepository` 조회 후 정보(`name`, `introduction`) 업데이트.
