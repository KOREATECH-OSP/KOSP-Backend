# 모집 공고 삭제 (Recruit Delete)

## 📡 API Specification
**`DELETE /v1/community/recruits/{id}`**

*   **Description**: 본인이 작성한(팀 리더) 모집 공고를 삭제합니다.
*   **Permission Name**: `recruit:delete`
*   **Permissions**: `USER` (팀 리더) or `ADMIN`

### Response
*   **204 No Content**
```json
// No Content
```

*   **403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "작성자만 삭제할 수 있습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.delete`
*   **Flow**:
1. Path ID로 공고 조회.
2. 삭제 권한(리더/관리자) 확인.
3. `is_deleted = true` 처리 (Soft Delete).
