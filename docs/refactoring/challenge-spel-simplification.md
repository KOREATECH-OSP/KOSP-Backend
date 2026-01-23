# Challenge SpEL 단순화 계획

## 현재 문제점

현재 Challenge 엔티티에 3개의 중복된 필드가 존재:

```java
String condition;      // SpEL 표현식 (달성 여부: Boolean)
String progressField;  // SpEL 표현식 (진행률 값: Number)
Integer maxProgress;   // 목표치 (분모)
```

### 문제 1: 단순 조건에서 중복
```json
{
  "condition": "totalCommits >= 100",
  "progressField": "totalCommits", 
  "maxProgress": 100
}
```
→ 같은 정보를 3번 표현

### 문제 2: 복합 조건에서 진행률 표현 불가
```json
{
  "condition": "totalCommits >= 10 && totalStarsReceived >= 10",
  "progressField": "???",
  "maxProgress": "???"
}
```
→ 어떤 값을 progressField로 선택해야 할지 모호

---

## 새로운 설계

### 핵심 아이디어
- **프론트엔드가 SpEL 표현식을 작성**
- **SpEL 평가 결과는 항상 백분율 (0~100)**
- **백엔드는 SpEL을 신뢰하고 평가만 수행**

### 필드 변경

| 기존 | 신규 | 설명 |
|------|------|------|
| `condition` | `condition` | 달성 여부 판단용 SpEL (Boolean 반환) |
| `progressField` | **삭제** | - |
| `maxProgress` | **삭제** | - |
| - | `progressExpression` | 진행률 계산용 SpEL (0~100 반환) |

### Entity 변경

```java
@Entity
public class Challenge {
    // 기존 유지
    private String name;
    private String description;
    private Integer tier;
    private String imageUrl;
    private Integer point;
    
    // 변경
    @Column(name = "`condition`", nullable = false, columnDefinition = "TEXT")
    private String condition;  // Boolean 반환 SpEL
    
    @Column(name = "progress_expression", nullable = false, columnDefinition = "TEXT")
    private String progressExpression;  // 0~100 반환 SpEL
    
    // 삭제: progressField, maxProgress
}
```

---

## SpEL 표현식 예시

### 1. 단순 조건 (커밋 100개 이상)
```json
{
  "condition": "totalCommits >= 100",
  "progressExpression": "T(Math).min(totalCommits * 100 / 100, 100)"
}
```
→ 커밋 75개일 때: 진행률 75%

### 2. 복합 조건 AND (커밋 10개 + 스타 10개)
```json
{
  "condition": "totalCommits >= 10 && totalStarsReceived >= 10",
  "progressExpression": "(T(Math).min(totalCommits, 10) + T(Math).min(totalStarsReceived, 10)) * 100 / 20"
}
```
→ 커밋 8개, 스타 6개일 때: (8 + 6) / 20 * 100 = 70%

### 3. 복합 조건 OR (PR 5개 또는 이슈 5개)
```json
{
  "condition": "totalPrs >= 5 || totalIssues >= 5",
  "progressExpression": "T(Math).max(T(Math).min(totalPrs * 100 / 5, 100), T(Math).min(totalIssues * 100 / 5, 100))"
}
```
→ 더 높은 쪽 진행률 표시

### 4. 점수 기반 (영향력 3점 이상)
```json
{
  "condition": "impactScore >= 3",
  "progressExpression": "T(Math).min(impactScore * 100 / 3, 100)"
}
```

---

## API 변경

### 챌린지 생성/수정 Request
```json
// POST /v1/admin/challenges
{
  "name": "커밋 마스터",
  "description": "100개의 커밋을 달성하세요",
  "condition": "totalCommits >= 100",
  "progressExpression": "T(Math).min(totalCommits * 100 / 100, 100)",
  "tier": 2,
  "point": 500,
  "imageUrl": "https://..."
}
```

### 챌린지 목록 조회 Response
```json
// GET /v1/challenges
{
  "challenges": [
    {
      "id": 1,
      "title": "커밋 마스터",
      "description": "100개의 커밋을 달성하세요",
      "progress": 75,        // 0~100 (백분율)
      "isCompleted": false,
      "tier": 2,
      "point": 500
    }
  ],
  "summary": {
    "totalChallenges": 10,
    "completedCount": 3,
    "overallProgress": 30.0,
    "totalEarnedPoints": 1500
  }
}
```

### SpEL 변수 목록 조회 (관리자용)
```json
// GET /v1/admin/challenges/spel-variables
{
  "variables": [
    { "name": "totalCommits", "description": "총 커밋 수", "type": "Integer" },
    { "name": "totalPrs", "description": "총 PR 수", "type": "Integer" },
    ...
  ],
  "helpers": [
    { "syntax": "T(Math).min(a, b)", "description": "a, b 중 작은 값" },
    { "syntax": "T(Math).max(a, b)", "description": "a, b 중 큰 값" }
  ],
  "examples": [
    {
      "name": "단순 조건",
      "condition": "totalCommits >= 100",
      "progressExpression": "T(Math).min(totalCommits * 100 / 100, 100)"
    },
    {
      "name": "복합 AND 조건",
      "condition": "totalCommits >= 10 && totalStarsReceived >= 10",
      "progressExpression": "(T(Math).min(totalCommits, 10) + T(Math).min(totalStarsReceived, 10)) * 100 / 20"
    }
  ]
}
```

---

## 백엔드 변경사항

### 1. Entity 수정
- `Challenge.java`: `progressField`, `maxProgress` 삭제, `progressExpression` 추가

### 2. DTO 수정
- `ChallengeRequest.java`: 필드 변경
- `ChallengeListResponse.java`: `current`, `total` → `progress` (Integer, 0~100)

### 3. Service 수정

**ChallengeService.getChallenges()**
```java
// 변경 전
int current = historyOpt.map(ChallengeHistory::getCurrentProgress).orElse(0);
int total = historyOpt.map(ChallengeHistory::getTargetProgress).orElse(challenge.getMaxProgress());

// 변경 후
int progress = calculateProgress(challenge, userStats);  // 0~100
```

**ChallengeEvaluator.calculateProgress()**
```java
private int calculateProgress(Challenge challenge, StandardEvaluationContext context) {
    Expression exp = parser.parseExpression(challenge.getProgressExpression());
    Object value = exp.getValue(context);
    
    if (value instanceof Number number) {
        int progress = number.intValue();
        return Math.max(0, Math.min(progress, 100));  // 0~100 범위 보장
    }
    return 0;
}
```

### 4. DB Migration
```sql
-- V7__challenge_spel_simplification.sql

ALTER TABLE challenge 
  ADD COLUMN progress_expression TEXT NOT NULL DEFAULT 'T(Math).min(totalCommits * 100 / 100, 100)';

-- 기존 데이터 마이그레이션 (progressField + maxProgress → progressExpression)
UPDATE challenge 
SET progress_expression = CONCAT('T(Math).min(', progress_field, ' * 100 / ', max_progress, ', 100)');

ALTER TABLE challenge 
  DROP COLUMN progress_field,
  DROP COLUMN max_progress;
```

### 5. ChallengeHistory 변경
```java
// 변경 전
private Integer currentProgress;
private Integer targetProgress;

// 변경 후
private Integer progressAtAchievement;  // 달성 시점의 진행률 (항상 100)
```

---

## 프론트엔드 가이드

### 챌린지 생성 UI
```
┌─────────────────────────────────────────────────────┐
│ 챌린지 생성                                          │
├─────────────────────────────────────────────────────┤
│ 이름: [커밋 마스터                              ]    │
│ 설명: [100개의 커밋을 달성하세요                ]    │
│                                                     │
│ ▼ 조건 유형 선택                                    │
│ ┌─────────────────────────────────────────────────┐ │
│ │ ○ 단순 조건 (필드 하나)                         │ │
│ │ ○ 복합 조건 (커스텀 SpEL)                       │ │
│ └─────────────────────────────────────────────────┘ │
│                                                     │
│ [단순 조건 선택 시]                                  │
│ 필드: [totalCommits ▼]  조건: [>= ▼]  값: [100]    │
│ → 자동 생성:                                        │
│   condition: "totalCommits >= 100"                 │
│   progressExpression: "T(Math).min(totalCommits * 100 / 100, 100)" │
│                                                     │
│ [복합 조건 선택 시]                                  │
│ 달성 조건 (SpEL):                                   │
│ [totalCommits >= 10 && totalStarsReceived >= 10]   │
│                                                     │
│ 진행률 계산식 (SpEL, 0~100 반환):                   │
│ [(T(Math).min(totalCommits, 10) + ...]             │
│                                                     │
│ 티어: [2 ▼]   포인트: [500]                         │
└─────────────────────────────────────────────────────┘
```

### 진행률 표시
```jsx
// 백엔드에서 받은 progress (0~100)
<ProgressBar value={challenge.progress} max={100} />
<span>{challenge.progress}%</span>
```

---

## 마이그레이션 순서

1. **DB Migration 실행** (V7)
2. **Entity, DTO 수정**
3. **Service 로직 수정**
4. **기존 챌린지 데이터 progressExpression 검증**
5. **프론트엔드 배포**
6. **테스트**

---

## 고려사항

### SpEL 보안
- 사용 가능한 함수 화이트리스트: `T(Math).min`, `T(Math).max`
- 위험한 표현식 차단 (Runtime, System 등)

### 진행률 범위 보장
```java
int progress = Math.max(0, Math.min(evaluatedValue, 100));
```

### 프론트 SpEL 빌더 제공
- 단순 조건은 UI로 선택 → SpEL 자동 생성
- 복합 조건은 직접 입력 + 템플릿 제공
