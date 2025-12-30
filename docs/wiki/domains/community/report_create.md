# 게시글 신고 (Article Report)

## 📡 API Specification
**`POST /v1/community/articles/{articleId}/reports`**

*   **Description**: 부적절한 게시글을 관리자에게 신고합니다.
*   **Permission Name**: `community:article:report`
*   **Permissions**: `USER`

### Request
```json
{
  "reason": "욕설/비하 발언",
  "description": "본문에 심한 욕설이 포함되어 있습니다."
}
```

### Response
*   **201 Created**
```json
// No Content
```

*   **400 Bad Request**
```json
{
  "code": "ALREADY_REPORTED",
  "message": "이미 신고한 게시글입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `ReportController.reportArticle`
*   **Flow**:
1. `ArticleRepository` 게시글 확인.
2. `ReportRepository` 중복 신고 여부 확인 (User-Article).
3. `Report` 엔티티 생성 및 저장.
4. 일정 횟수 이상 누적 시 자동 Blind 처리 로직 (Optional).
