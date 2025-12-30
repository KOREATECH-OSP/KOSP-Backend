# 프로필 조회 (Get Profile)

## 📡 API Specification
**`GET /v1/users/{userId}`**

*   **Description**: 특정 사용자의 공개 프로필 정보를 조회합니다.
*   **Permission Name**: `user:profile:read`
*   **Permissions**: `ANONYMOUS` (공개 프로필)

### Response
*   **200 OK**
```json
{
  "id": 1,
  "nickname": "spartacoding",
  "profileImageUrl": "https://kosp.s3.amazonaws.com/...",
  "githubId": "octocat",
  "tier": 5
  "name": "홍길동",
  ...
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
*   **Controller**: `UserController.getProfile`
*   **Service**: `UserService.getProfile`
*   **Flow**:
1. `UserRepository`에서 ID로 사용자 조회 (없을 시 Exception).
2. `UserProfileResponse` DTO 변환 및 반환.
3. (민감정보 제외) 이메일, 학번 등은 본인 조회 시에만 포함되거나 별도 API로 분리.
