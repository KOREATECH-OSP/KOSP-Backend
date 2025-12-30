# 사용자 역할 변경 (Admin User Role Update)

## 📡 API Specification
**`PUT /v1/admin/users/{userId}/roles`**

*   **Description**: 관리자 권한으로 사용자의 역할(Role)을 변경합니다.
*   **Permission Name**: `admin:users:update-roles`
*   **Permissions**: `ADMIN`

### Request
```json
{
  "roles": ["USER", "ADMIN"]
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
*   **Controller**: `AdminController.updateUserRoles`
*   **Service**: `AdminMemberService.updateUserRoles`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. `UserRepository` 사용자 조회.
3. `RoleRepository`에서 요청된 역할 이름들 조회 및 검증.
4. 사용자 역할 목록 초기화 후 새로운 역할 할당.
5. `PermissionAdminService`를 통해 권한 변경 이벤트 발행 (Redis 세션 등 갱신 트리거).
