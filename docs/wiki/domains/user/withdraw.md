# 회원 탈퇴 (Withdrawal)

## 📡 API Specification
**`DELETE /v1/users/{userId}`**

*   **Description**: 회원을 탈퇴 처리합니다. (데이터 Soft Delete 적용)
*   **Permission Name**: `user:withdraw`
*   **Permissions**: `USER` (본인) or `ADMIN`

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
  "message": "권한이 없습니다 (본인만 탈퇴 가능)."
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
*   **Controller**: `UserController.delete`
*   **Service**: `UserService.delete`
*   **Flow**:
1. 요청한 유저(`AuthUser`)와 대상 유저 ID 일치 여부 확인.
2. `UserService.delete()` 호출.
3. **Soft Delete**: `isDeleted = true`, `roles` 제거 등.
4. (Optional) 리프레시 토큰 등 보안 정보 정리.
