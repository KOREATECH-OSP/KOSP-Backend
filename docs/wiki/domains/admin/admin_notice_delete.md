# 공지사항 삭제 (Admin Notice Delete)

## 📡 API Specification
**`DELETE /v1/admin/notices/{noticeId}`**

*   **Description**: 등록된 공지사항을 삭제합니다.
*   **Permission Name**: `admin:notice:delete`
*   **Permissions**: `ADMIN`

### Response
*   **204 No Content**
```json
// No Content
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.deleteNotice`
*   **Flow**:
1. Path ID로 조회 후 삭제(Soft Delete).
