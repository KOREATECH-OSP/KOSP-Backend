# 사용자 삭제 (Admin User Delete)

## 📡 API Specification
**`DELETE /v1/admin/users/{userId}`**

*   **Description**: 관리자 권한으로 사용자를 강제 탈퇴(Soft Delete) 시킵니다.
*   **Permission Name**: `admin:user:delete`
*   **Permissions**: `ADMIN`

### Response
*   **204 No Content**
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
*   **Controller**: `AdminController.deleteUser`
*   **Service**: `AdminMemberService.deleteUser`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. `UserRepository`에서 사용자 조회.
3. `User.delete()` 호출하여 Soft Delete 처리 (`isDeleted = true`).
2. `is_deleted = true` 처리.
3. 관련 리소스(토큰 등) 정리.
