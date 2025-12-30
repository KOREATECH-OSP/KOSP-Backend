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

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "본인의 프로필만 수정할 수 있습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `UserController.update`
*   **Flow**:
1. PathVariable `userId`와 현재 로그인 사용자 ID 비교.
2. 불일치 시 `GlobalException(FORBIDDEN)` 발생.
3. 닉네임 중복 검사 (변경 시).
4. `User` 엔티티 업데이트.
