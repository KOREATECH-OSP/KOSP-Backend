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
  "code": "ARTICLE_NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.deleteNotice`
*   **Service**: `AdminContentService.deleteNotice`
*   **Flow**:
1. 관리자 권한(`ADMIN`) 검증.
2. `ArticleRepository`에서 ID로 게시글 조회.
3. `Article.delete()` 호출하여 Soft Delete 처리.
