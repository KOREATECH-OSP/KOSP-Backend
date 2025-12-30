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
  "code": "VALIDATION_ERROR",
  "message": "신고 사유는 필수입니다."
}
```

*   **401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다."
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
*   **Controller**: `ReportController.reportArticle`
*   **Service**: `ReportService.reportArticle`
*   **Flow**:
1. `ArticleRepository`에서 게시글 존재 여부 확인 (없을 시 404).
2. `ReportRepository` 중복 신고 여부 확인 (User-Article).
3. `Report` 엔티티 생성 (TargetType=ARTICLE, Status=PENDING).
4. `ReportRepository.save()` 호출.
5. 일정 횟수 이상 누적 시 자동 Blind 처리 로직 (Optional).
