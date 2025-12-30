# 챌린지 생성 (Admin Challenge Create)

## 📡 API Specification
**`POST /v1/admin/challenges`**

*   **Description**: 관리자 권한으로 새로운 챌린지를 생성합니다.
*   **Permission Name**: `admin:challenge:create`
*   **Permissions**: `ADMIN`

### Request
```json
{
  "name": "commits-100",
  "description": "총 커밋 100개 달성",
  "tier": 1,
  "condition": "user.totalCommits >= 100", // SpEL Expression
  "imageUrl": "..."
}
```

### Response
*   **201 Created**
```json
// No Content
```

---

## 🛠️ Implementation Details
*   **Controller**: `AdminController.createChallenge`
*   **Flow**:
1. SpEL 조건식 문법 유효성 검증.
2. `Challenge` 엔티티 생성 및 저장.
