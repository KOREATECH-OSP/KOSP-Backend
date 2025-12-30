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

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.deleteUser`
*   **Flow**:
1. Path ID로 대상 사용자 조회.
2. `is_deleted = true` 처리.
3. 관련 리소스(토큰 등) 정리.
