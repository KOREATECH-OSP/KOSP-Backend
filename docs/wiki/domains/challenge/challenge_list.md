# 챌린지 목록 조회 (Challenge List)

## 📡 API Specification
**`GET /v1/challenges`**

*   **Description**: 모든 도전 과제 목록과 현재 사용자의 달성 현황을 조회합니다.
*   **Permission Name**: `challenge:list`
*   **Permissions**: `USER`

### Response
*   **200 OK**
```json
{
  "challenges": [
    {
      "id": 1,
      "name": "commits-100",
      "description": "총 커밋 100개 달성",
      "tier": 1,
      "isAchieved": true,
      "achievedAt": "2024-12-01T10:00:00"
    },
    {
      "id": 2,
      "name": "pull-requests-50",
      "description": "PR 50회 머지",
      "tier": 2,
      "isAchieved": false,
      "achievedAt": null
    }
  ]
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `ChallengeController.getChallenges`
*   **Flow**:
1. `ChallengeRepository` 전체 목록 조회 (Caching 권장).
2. `ChallengeHistoryRepository`에서 현재 유저의 달성 기록 조회.
3. 두 데이터를 병합하여 `isAchieved` 마킹 후 반환.
