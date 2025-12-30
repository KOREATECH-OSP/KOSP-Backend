# 활동 평가 (Challenge Evaluate)

## 📡 API Specification
**`POST`** *(Planned)*

*   **Description**: 사용자의 활동(Commits, PR 등)을 기반으로 달성 가능한 챌린지를 평가하고 갱신합니다.
*   **Permissions**: `SYSTEM` (Scheduler) or `USER` (Trigger)

### Note
현재는 별도의 API 엔드포인트가 정의되지 않았으며, `Github Analysis` 배치 작업 혹은 이벤트 발생 시 내부적으로 실행될 예정입니다.

---

## 🛠️ Implementation Details
*   **Service**: `ChallengeEvaluationService`
*   **Logic**:
1. SpEL(Spring Expression Language)을 사용하여 각 챌린지의 조건식(`condition`)을 평가.
2. 예: `user.totalCommits >= 100` -> True면 `ChallengeHistory` 저장.
