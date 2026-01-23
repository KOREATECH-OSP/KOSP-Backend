# GitHub 챌린지 데이터 정리 (SpEL 포함)

## ⚠️ 필드명 매핑

문서의 조건 필드명과 실제 `GithubUserStatistics` 엔티티 필드명 매핑:

| 문서 필드명 | 실제 SpEL 필드명 |
|-------------|------------------|
| `totalCommitCount` | `totalCommits` |
| `totalPrCount` | `totalPrs` |
| `totalIssueCount` | `totalIssues` |
| `contributedRepoCount` | `contributedReposCount` |
| `userStarCount` | `totalStarsReceived` |

---

## 🥉 브론즈 (Tier: 1)

| name | description | condition | progressField | maxProgress | point |
|------|-------------|-----------|---------------|-------------|-------|
| 첫 커밋의 설렘 | GitHub에 첫 커밋을 진행해보세요. | `totalCommits >= 1` | `totalCommits` | 1 | 100 |
| 커밋 10개 달성 | GitHub에 커밋을 10개 진행해보세요. | `totalCommits >= 10` | `totalCommits` | 10 | 100 |
| 첫 PR 도전 | 첫 번째 Pull Request를 생성해보세요. | `totalPrs >= 1` | `totalPrs` | 1 | 100 |
| 이슈 리포터 | 첫 번째 이슈를 등록해보세요. | `totalIssues >= 1` | `totalIssues` | 1 | 100 |
| 레포 탐험가 | 1개 이상의 레포지토리에 기여해보세요. | `contributedReposCount >= 1` | `contributedReposCount` | 1 | 100 |

---

## 🥈 실버 (Tier: 2)

| name | description | condition | progressField | maxProgress | point |
|------|-------------|-----------|---------------|-------------|-------|
| 꾸준한 커밋러 | 커밋 50개를 달성해보세요. | `totalCommits >= 50` | `totalCommits` | 50 | 200 |
| PR 마스터 입문 | Pull Request 5개를 생성해보세요. | `totalPrs >= 5` | `totalPrs` | 5 | 200 |
| 이슈 헌터 | 이슈 5개를 등록해보세요. | `totalIssues >= 5` | `totalIssues` | 5 | 200 |
| 코드 기여자 | 1,000줄 이상의 코드를 추가해보세요. | `totalAdditions >= 1000` | `totalAdditions` | 1000 | 200 |
| 다양한 기여 | 3개 이상의 레포지토리에 기여해보세요. | `contributedReposCount >= 3` | `contributedReposCount` | 3 | 200 |

---

## 🥇 골드 (Tier: 3)

| name | description | condition | progressField | maxProgress | point |
|------|-------------|-----------|---------------|-------------|-------|
| 커밋 장인 | 커밋 200개를 달성해보세요. | `totalCommits >= 200` | `totalCommits` | 200 | 300 |
| PR 전문가 | Pull Request 20개를 생성해보세요. | `totalPrs >= 20` | `totalPrs` | 20 | 300 |
| 적극적인 소통가 | 이슈 20개를 등록해보세요. | `totalIssues >= 20` | `totalIssues` | 20 | 300 |
| 코드 빌더 | 5,000줄 이상의 코드를 추가해보세요. | `totalAdditions >= 5000` | `totalAdditions` | 5000 | 300 |
| 레포 여행자 | 5개 이상의 레포지토리에 기여해보세요. | `contributedReposCount >= 5` | `contributedReposCount` | 5 | 300 |

---

## 💎 플래티넘 (Tier: 4)

| name | description | condition | progressField | maxProgress | point |
|------|-------------|-----------|---------------|-------------|-------|
| 커밋 매니아 | 커밋 500개를 달성해보세요. | `totalCommits >= 500` | `totalCommits` | 500 | 500 |
| PR 리더 | Pull Request 50개를 생성해보세요. | `totalPrs >= 50` | `totalPrs` | 50 | 500 |
| 이슈 마스터 | 이슈 50개를 등록해보세요. | `totalIssues >= 50` | `totalIssues` | 50 | 500 |
| 스타 콜렉터 | 내 레포지토리에 스타 10개를 받아보세요. | `totalStarsReceived >= 10` | `totalStarsReceived` | 10 | 500 |
| 오픈소스 기여자 | 10개 이상의 레포지토리에 기여해보세요. | `contributedReposCount >= 10` | `contributedReposCount` | 10 | 500 |

---

## 💠 다이아몬드 (Tier: 5)

| name | description | condition | progressField | maxProgress | point |
|------|-------------|-----------|---------------|-------------|-------|
| 커밋 히어로 | 커밋 1,000개를 달성해보세요. | `totalCommits >= 1000` | `totalCommits` | 1000 | 800 |
| PR 마에스트로 | Pull Request 100개를 생성해보세요. | `totalPrs >= 100` | `totalPrs` | 100 | 800 |
| 코드 아키텍트 | 20,000줄 이상의 코드를 추가해보세요. | `totalAdditions >= 20000` | `totalAdditions` | 20000 | 800 |
| 인기 개발자 | 내 레포지토리에 스타 50개를 받아보세요. | `totalStarsReceived >= 50` | `totalStarsReceived` | 50 | 800 |
| 멀티 컨트리뷰터 | 20개 이상의 레포지토리에 기여해보세요. | `contributedReposCount >= 20` | `contributedReposCount` | 20 | 800 |

---

## 🔴 루비 (Tier: 6)

| name | description | condition | progressField | maxProgress | point |
|------|-------------|-----------|---------------|-------------|-------|
| 커밋 레전드 | 커밋 3,000개를 달성해보세요. | `totalCommits >= 3000` | `totalCommits` | 3000 | 1000 |
| PR 그랜드마스터 | Pull Request 300개를 생성해보세요. | `totalPrs >= 300` | `totalPrs` | 300 | 1000 |
| 코드 거장 | 50,000줄 이상의 코드를 추가해보세요. | `totalAdditions >= 50000` | `totalAdditions` | 50000 | 1000 |
| 스타 인플루언서 | 내 레포지토리에 스타 200개를 받아보세요. | `totalStarsReceived >= 200` | `totalStarsReceived` | 200 | 1000 |
| 오픈소스 마스터 | 50개 이상의 레포지토리에 기여해보세요. | `contributedReposCount >= 50` | `contributedReposCount` | 50 | 1000 |

---

## JSON 형식 (API 요청용)

```json
[
  {"name": "첫 커밋의 설렘", "description": "GitHub에 첫 커밋을 진행해보세요.", "condition": "totalCommits >= 1", "progressField": "totalCommits", "maxProgress": 1, "tier": 1, "point": 100, "imageUrl": null},
  {"name": "커밋 10개 달성", "description": "GitHub에 커밋을 10개 진행해보세요.", "condition": "totalCommits >= 10", "progressField": "totalCommits", "maxProgress": 10, "tier": 1, "point": 100, "imageUrl": null},
  {"name": "첫 PR 도전", "description": "첫 번째 Pull Request를 생성해보세요.", "condition": "totalPrs >= 1", "progressField": "totalPrs", "maxProgress": 1, "tier": 1, "point": 100, "imageUrl": null},
  {"name": "이슈 리포터", "description": "첫 번째 이슈를 등록해보세요.", "condition": "totalIssues >= 1", "progressField": "totalIssues", "maxProgress": 1, "tier": 1, "point": 100, "imageUrl": null},
  {"name": "레포 탐험가", "description": "1개 이상의 레포지토리에 기여해보세요.", "condition": "contributedReposCount >= 1", "progressField": "contributedReposCount", "maxProgress": 1, "tier": 1, "point": 100, "imageUrl": null},

  {"name": "꾸준한 커밋러", "description": "커밋 50개를 달성해보세요.", "condition": "totalCommits >= 50", "progressField": "totalCommits", "maxProgress": 50, "tier": 2, "point": 200, "imageUrl": null},
  {"name": "PR 마스터 입문", "description": "Pull Request 5개를 생성해보세요.", "condition": "totalPrs >= 5", "progressField": "totalPrs", "maxProgress": 5, "tier": 2, "point": 200, "imageUrl": null},
  {"name": "이슈 헌터", "description": "이슈 5개를 등록해보세요.", "condition": "totalIssues >= 5", "progressField": "totalIssues", "maxProgress": 5, "tier": 2, "point": 200, "imageUrl": null},
  {"name": "코드 기여자", "description": "1,000줄 이상의 코드를 추가해보세요.", "condition": "totalAdditions >= 1000", "progressField": "totalAdditions", "maxProgress": 1000, "tier": 2, "point": 200, "imageUrl": null},
  {"name": "다양한 기여", "description": "3개 이상의 레포지토리에 기여해보세요.", "condition": "contributedReposCount >= 3", "progressField": "contributedReposCount", "maxProgress": 3, "tier": 2, "point": 200, "imageUrl": null},

  {"name": "커밋 장인", "description": "커밋 200개를 달성해보세요.", "condition": "totalCommits >= 200", "progressField": "totalCommits", "maxProgress": 200, "tier": 3, "point": 300, "imageUrl": null},
  {"name": "PR 전문가", "description": "Pull Request 20개를 생성해보세요.", "condition": "totalPrs >= 20", "progressField": "totalPrs", "maxProgress": 20, "tier": 3, "point": 300, "imageUrl": null},
  {"name": "적극적인 소통가", "description": "이슈 20개를 등록해보세요.", "condition": "totalIssues >= 20", "progressField": "totalIssues", "maxProgress": 20, "tier": 3, "point": 300, "imageUrl": null},
  {"name": "코드 빌더", "description": "5,000줄 이상의 코드를 추가해보세요.", "condition": "totalAdditions >= 5000", "progressField": "totalAdditions", "maxProgress": 5000, "tier": 3, "point": 300, "imageUrl": null},
  {"name": "레포 여행자", "description": "5개 이상의 레포지토리에 기여해보세요.", "condition": "contributedReposCount >= 5", "progressField": "contributedReposCount", "maxProgress": 5, "tier": 3, "point": 300, "imageUrl": null},

  {"name": "커밋 매니아", "description": "커밋 500개를 달성해보세요.", "condition": "totalCommits >= 500", "progressField": "totalCommits", "maxProgress": 500, "tier": 4, "point": 500, "imageUrl": null},
  {"name": "PR 리더", "description": "Pull Request 50개를 생성해보세요.", "condition": "totalPrs >= 50", "progressField": "totalPrs", "maxProgress": 50, "tier": 4, "point": 500, "imageUrl": null},
  {"name": "이슈 마스터", "description": "이슈 50개를 등록해보세요.", "condition": "totalIssues >= 50", "progressField": "totalIssues", "maxProgress": 50, "tier": 4, "point": 500, "imageUrl": null},
  {"name": "스타 콜렉터", "description": "내 레포지토리에 스타 10개를 받아보세요.", "condition": "totalStarsReceived >= 10", "progressField": "totalStarsReceived", "maxProgress": 10, "tier": 4, "point": 500, "imageUrl": null},
  {"name": "오픈소스 기여자", "description": "10개 이상의 레포지토리에 기여해보세요.", "condition": "contributedReposCount >= 10", "progressField": "contributedReposCount", "maxProgress": 10, "tier": 4, "point": 500, "imageUrl": null},

  {"name": "커밋 히어로", "description": "커밋 1,000개를 달성해보세요.", "condition": "totalCommits >= 1000", "progressField": "totalCommits", "maxProgress": 1000, "tier": 5, "point": 800, "imageUrl": null},
  {"name": "PR 마에스트로", "description": "Pull Request 100개를 생성해보세요.", "condition": "totalPrs >= 100", "progressField": "totalPrs", "maxProgress": 100, "tier": 5, "point": 800, "imageUrl": null},
  {"name": "코드 아키텍트", "description": "20,000줄 이상의 코드를 추가해보세요.", "condition": "totalAdditions >= 20000", "progressField": "totalAdditions", "maxProgress": 20000, "tier": 5, "point": 800, "imageUrl": null},
  {"name": "인기 개발자", "description": "내 레포지토리에 스타 50개를 받아보세요.", "condition": "totalStarsReceived >= 50", "progressField": "totalStarsReceived", "maxProgress": 50, "tier": 5, "point": 800, "imageUrl": null},
  {"name": "멀티 컨트리뷰터", "description": "20개 이상의 레포지토리에 기여해보세요.", "condition": "contributedReposCount >= 20", "progressField": "contributedReposCount", "maxProgress": 20, "tier": 5, "point": 800, "imageUrl": null},

  {"name": "커밋 레전드", "description": "커밋 3,000개를 달성해보세요.", "condition": "totalCommits >= 3000", "progressField": "totalCommits", "maxProgress": 3000, "tier": 6, "point": 1000, "imageUrl": null},
  {"name": "PR 그랜드마스터", "description": "Pull Request 300개를 생성해보세요.", "condition": "totalPrs >= 300", "progressField": "totalPrs", "maxProgress": 300, "tier": 6, "point": 1000, "imageUrl": null},
  {"name": "코드 거장", "description": "50,000줄 이상의 코드를 추가해보세요.", "condition": "totalAdditions >= 50000", "progressField": "totalAdditions", "maxProgress": 50000, "tier": 6, "point": 1000, "imageUrl": null},
  {"name": "스타 인플루언서", "description": "내 레포지토리에 스타 200개를 받아보세요.", "condition": "totalStarsReceived >= 200", "progressField": "totalStarsReceived", "maxProgress": 200, "tier": 6, "point": 1000, "imageUrl": null},
  {"name": "오픈소스 마스터", "description": "50개 이상의 레포지토리에 기여해보세요.", "condition": "contributedReposCount >= 50", "progressField": "contributedReposCount", "maxProgress": 50, "tier": 6, "point": 1000, "imageUrl": null}
]
```

---

## 티어별 요약

| 티어 | 이름 | 챌린지 수 | 포인트 | 총 획득 가능 포인트 |
|------|------|-----------|--------|---------------------|
| 1 | 🥉 브론즈 | 5 | 100 | 500 |
| 2 | 🥈 실버 | 5 | 200 | 1,000 |
| 3 | 🥇 골드 | 5 | 300 | 1,500 |
| 4 | 💎 플래티넘 | 5 | 500 | 2,500 |
| 5 | 💠 다이아몬드 | 5 | 800 | 4,000 |
| 6 | 🔴 루비 | 5 | 1,000 | 5,000 |
| **합계** | | **30** | | **14,500** |

---

## 사용 가능한 SpEL 변수 (GithubUserStatistics)

| 변수명 | 타입 | 설명 |
|--------|------|------|
| `totalCommits` | Integer | 총 커밋 수 |
| `totalLines` | Integer | 총 라인 수 |
| `totalAdditions` | Integer | 총 추가 라인 수 |
| `totalDeletions` | Integer | 총 삭제 라인 수 |
| `totalPrs` | Integer | 총 PR 수 |
| `totalIssues` | Integer | 총 이슈 수 |
| `ownedReposCount` | Integer | 소유 레포지토리 수 |
| `contributedReposCount` | Integer | 기여 레포지토리 수 |
| `totalStarsReceived` | Integer | 받은 스타 수 |
| `totalForksReceived` | Integer | 받은 포크 수 |
| `nightCommits` | Integer | 야간 커밋 수 (22시~06시) |
| `dayCommits` | Integer | 주간 커밋 수 |
| `activityScore` | Decimal | 활동 점수 (0~3) |
| `diversityScore` | Decimal | 다양성 점수 (0~1) |
| `impactScore` | Decimal | 영향력 점수 (0~5) |
| `totalScore` | Decimal | 총 점수 |
