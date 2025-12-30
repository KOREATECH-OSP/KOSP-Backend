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
    *   Headers: `Location: /v1/community/articles/{id}`
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "제목은 필수입니다."
}
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
  "code": "BOARD_NOT_FOUND",
  "message": "공지사항 게시판을 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.createNotice`
*   **Service**: `AdminContentService.createNotice`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. `BoardRepository`에서 이름이 "공지사항" 또는 "NOTICE"인 게시판 조회.
3. `Article` 엔티티 생성 (Category=NOTICE, isPinned=true/false).
4. `ArticleRepository.save()` 호출.설정 후 저장.
