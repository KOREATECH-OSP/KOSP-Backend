# 공지사항 작성 (Admin Notice Create)

## 📡 API Specification
**`POST /v1/admin/notices`**

*   **Description**: 시스템 전체 공지사항을 등록합니다.
*   **Permission Name**: `admin:notice:create`
*   **Permissions**: `ADMIN`

### Request
```json
{
  "title": "[점검] 12월 30일 서버 점검 안내",
  "content": "...",
  "isPinned": true,
  "targetScope": "ALL"
}
```

### Response
*   **201 Created**
```json
// No Content
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.createNotice`
*   **Flow**:
1. `Notice` 엔티티(또는 Article Type=NOTICE) 생성.
2. `isPinned` 등 옵션 설정 후 저장.
