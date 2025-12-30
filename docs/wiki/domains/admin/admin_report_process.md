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

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.processReport`
*   **Flow**:
1. Path ID로 신고 내역 조회.
2. `action`에 따라 로직 분기:
    *   `DELETE_CONTENT`: 대상 게시글/댓글 삭제.
    *   `BAN_USER`: 대상 작성자 정지.
    *   `REJECT`: 신고 기각 (상태만 변경).
3. 신고 상태(`status`)를 `PROCESSED`로 업데이트.
