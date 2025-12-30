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
  "code": "RECRUIT_NOT_FOUND",
  "message": "모집 공고를 찾을 수 없습니다."
}
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
*   **Controller**: `RecruitController.apply`
*   **Flow**:
1. `RecruitRepository`에서 공고 ID로 조회.
2. 이미 지원했는지 여부 확인 (Optional).
3. `RecruitApply` 엔티티 생성 및 저장.
4. 팀 리더에게 알림 전송 (Optional).
