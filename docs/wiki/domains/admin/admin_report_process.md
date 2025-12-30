# 신고 처리 (Admin Report Process)

## 📡 API Specification
**`POST /v1/admin/reports/{reportId}`**

*   **Description**: 신고 내용을 검토하고 승인(제재) 또는 반려 처리합니다.
*   **Permission Name**: `admin:report:process`
*   **Permissions**: `ADMIN`

### Request
```json
{
  "action": "DELETE_CONTENT" // or "BAN_USER", "REJECT", "BLIND"
}
```

### Response
*   **200 OK**
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "BAD_REQUEST",
  "message": "이미 처리된 신고입니다."
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
  "code": "REPORT_NOT_FOUND",
  "message": "신고 내용을 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.processReport`
*   **Service**: `AdminReportService.processReport`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. 신고 ID 조회 및 `PENDING` 상태 확인 (아닐 경우 400 에러).
3. **삭제 승인(DELETE_CONTENT)**:
    *   TargetType 확인 (`ARTICLE` or `COMMENT`).
    *   `AdminContentService`를 통해 대상 콘텐츠 Soft Delete.
    *   신고 상태 `ACCEPTED`로 변경.
4. **기각(REJECT)**:
    *   신고 상태 `REJECTED`로 변경.
