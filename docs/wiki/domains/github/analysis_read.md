# GitHub 활동 분석 (GitHub Analysis)

## 📡 API Specification
**`GET /v1/github/users/{username}/analysis`**

*   **Description**: 특정 유저의 GitHub 활동(언어 사용량, 커밋 시간대, 기여도 등)을 분석하여 조회합니다.
*   **Permission Name**: `github:analysis:read`
*   **Permissions**: `ANONYMOUS` (or `USER`)

### Response
*   **200 OK**
```json
{
  "githubId": "octocat",
  "totalStars": 150,
  "totalCommits": 1200,
  "languages": {
    "Java": 60.5,
    "Python": 30.2
  },
  "activityHeatmap": [ ... ],
  "updatedAt": "2025-01-01T12:00:00"
}
```

---

## 🛠️ Implementation Details
*   **Controller**: `GithubController.getGithubAnalysis`
*   **Flow**:
1. MongoDB `GithubAnalysis` 컬렉션에서 해당 유저의 최근 분석 데이터 조회.
2. 데이터가 없거나 오래된 경우 `GithubApiClient`를 통해 GitHub GraphQL API 호출 (비동기 갱신 고려).
3. 분석 결과를 가공하여 반환.
