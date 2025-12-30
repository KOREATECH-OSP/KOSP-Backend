# 사용자 정보 수정 (Admin User Update)

## 📡 API Specification
**`PUT /v1/admin/users/{userId}`**

*   **Description**: 관리자 권한으로 사용자의 기본 정보(이름, 소개 등)를 수정합니다.
*   **Permission Name**: `admin:users:update`
*   **Permissions**: `ADMIN`

### Request
```json
{
  "name": "홍길동",
  "introduction": "관리자에 의해 수정됨",
  "profileImageUrl": "https://..."
}
```

### Response
*   **200 OK**
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

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "접근 권한이 없습니다 (관리자 권한 필요)."
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
*   **Controller**: `AdminController.updateUser`
*   **Service**: `AdminMemberService.updateUser`
*   **Flow**:
1. `AdminApi` 인터페이스의 `@Permit` 어노테이션을 통해 관리자 권한(`ADMIN`) 검증.
2. `UserRepository`에서 `userId`로 대상 사용자 조회 (없을 시 예외 발생).
3. `User.updateInfo()` 호출하여 이름 및 자기소개 수정.
4. `GithubUser`가 존재하는 경우 프로필 이미지 URL 업데이트.
