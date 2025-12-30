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
  "message": "권한이 없습니다 (본인 작성 공고만 삭제 가능)."
}
```

*   **404 Not Found**
```json
{
  "code": "RECRUIT_NOT_FOUND",
  "message": "모집 공고를 찾을 수 없습니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.delete`
*   **Service**: `RecruitService.delete`
*   **Flow**:
1. `RecruitRepository`에서 ID로 공고 조회 (없을 시 404).
2. `validateOwner()`: 작성자 본인 확인 (아닐 경우 403).
3. `recruitRepository.delete()` 호출 (Hard Delete). 권한(리더/관리자) 확인.
3. `is_deleted = true` 처리 (Soft Delete).
