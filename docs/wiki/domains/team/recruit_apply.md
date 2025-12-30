# 모집 지원하기 (Recruit Apply)

## 📡 API Specification
**`POST /v1/community/recruits/{recruitId}/apply`**

*   **Description**: 사용자가 모집 공고를 보고 팀에 지원합니다.
*   **Permission Name**: `recruit:apply`
*   **Permissions**: `USER`

### Request
```json
{
  "reason": "열심히 하겠습니다!",
  "portfolioUrl": "https://github.com/my-repo"
}
```

### Response
*   **201 Created**
```json
// No Content
```

*   **409 Conflict**
```json
{
  "code": "ALREADY_APPLIED",
  "message": "이미 지원했거나 이미 팀원입니다."
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `RecruitController.applyRecruit`
*   **Flow**:
1. `RecruitRepository` 공고 조회.
2. 중복 지원/가입 여부 검증 (DB 조회).
3. `RecruitApply` 엔티티 생성.
4. 팀 리더에게 알림 발송 (Event).
