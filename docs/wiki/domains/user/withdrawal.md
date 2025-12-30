# 회원 탈퇴 (Withdrawal)

## 📡 API Specification
**`DELETE /v1/users/{userId}`**

*   **Description**: 회원을 탈퇴 처리합니다. (실제 데이터 삭제가 아닌 비활성화)
*   **Permissions**: `USER` (본인) or `ADMIN`

### Request
*   path variable: userId (user id)

---

## 🛠️ Implementation Details
*   **Soft Delete**: `is_deleted = true` UPDATE 쿼리 수행.
*   **Recovery**: 정책에 따라 유예 기간을 두거나, 즉시 재가입 불가 처리.
*   **Personal Data**: 민감 정보(비밀번호 등)는 파기하거나 마스킹 처리 고려.
