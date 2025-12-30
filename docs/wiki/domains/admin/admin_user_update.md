# 사용자 정보 수정 (Admin User Update)

## 📡 API Specification
**`PUT /v1/admin/users/{userId}`**

*   **Description**: 관리자 권한으로 사용자의 정보(Role, Status 등)를 강제로 수정합니다.
*   **Permission Name**: `admin:user:update`
*   **Permissions**: `ADMIN`

### Request
```json
{
  "role": "ADMIN", // 권한 승격/강등
  "status": "BANNED" // 계정 상태 변경
}
```

### Response
*   **200 OK**
```json
// No Content
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.updateUser`
*   **Flow**:
1. Path ID로 대상 사용자 조회.
2. 요청된 변경 사항 적용 (Role, Status).
3. 중요 변경 사항(권한 등)은 Audit Log 기록.
