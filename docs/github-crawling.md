# GitHub 크롤링 구조 문서

> Harvester 모듈의 GitHub 데이터 수집 전체 흐름, 상황별 동작, 누락 가능 지점, Rate Limit 분석

---

## 0. 한 줄 요약

> 사용자별로 GitHub API를 호출해서 커밋·PR·이슈·저장소를 MongoDB에 수집하고, 통계를 내서 점수를 계산하는 배치 시스템. 한 번 시작되면 완료 후 다음 실행을 스스로 예약하는 순환 구조다.

---

## 1. 관련 파일 목록

```
harvester/src/main/java/io/swkoreatech/kosp/
│
├── queue/
│   ├── GithubCollectionListener.java     ← 크롤링 시작점. RabbitMQ 메시지 수신
│   └── OrgCollectionListener.java        ← 조직 저장소 수집 (별도 흐름)
│
├── launcher/
│   └── PriorityJobLauncher.java          ← Spring Batch Job 실행기
│
├── job/
│   ├── GithubCollectionJobConfig.java    ← 9단계 파이프라인 정의
│   ├── JobSchedulingListener.java        ← Job 완료 후 다음 실행 예약
│   ├── StepCompletionListener.java       ← Step 완료 로깅
│   ├── StepMetricsListener.java          ← Step 메트릭 수집
│   ├── BatchMdcListener.java
│   └── ContextValidationListener.java
│
├── collection/step/impl/                 ← 실제 수집 로직 (핵심)
│   ├── RepositoryDiscoveryStep.java      ← Step 1: 저장소 목록 수집
│   ├── PullRequestMiningStep.java        ← Step 2: PR 수집
│   ├── IssueMiningStep.java              ← Step 3: 이슈 수집
│   ├── CommitMiningStep.java             ← Step 4: 커밋 수집
│   ├── StatisticsAggregationStep.java    ← Step 5: 통계 집계
│   ├── ScoreCalculationStep.java         ← Step 6: 점수 계산
│   ├── ChallengeEvaluationStep.java      ← Step 7: 챌린지 이벤트 발행
│   ├── PlatformAverageStep.java          ← Step 8: 전체 평균 계산
│   └── CleanupStep.java                  ← Step 9: 마무리 정리
│
├── collection/util/
│   ├── PaginationHelper.java             ← 커서 기반 페이지네이션 공통 처리
│   ├── TimeChunkGenerator.java           ← 월별 시간 청크 생성
│   ├── GraphQLErrorHandler.java          ← GraphQL 에러 분류
│   └── GraphQLErrorType.java             ← RETRYABLE / NON_RETRYABLE / PARTIAL
│
├── collection/document/                  ← MongoDB 저장 모델
│   ├── CollectionMetadataDocument.java   ← 마지막 수집 날짜 저장
│   ├── CommitDocument.java
│   ├── PullRequestDocument.java
│   ├── IssueDocument.java
│   └── ContributedRepoDocument.java
│
├── collection/repository/
│   ├── CollectionMetadataRepository.java
│   ├── CommitDocumentRepository.java
│   ├── ContributedRepoDocumentRepository.java
│   ├── GithubRepositoryStatisticsRepository.java
│   ├── IssueDocumentRepository.java
│   └── PullRequestDocumentRepository.java
│
├── client/
│   ├── GithubGraphQLClient.java          ← GitHub GraphQL API 호출 (메인)
│   ├── GithubRestApiClient.java          ← GitHub REST API 호출 (조직용)
│   ├── RateLimitManager.java             ← 포인트 잔량 추적·제어
│   └── RateLimitException.java
│
├── trigger/
│   └── PlatformAverageScheduler.java     ← 매시간 정각 평균 재계산
│
├── statistics/
│   ├── model/PlatformStatistics.java
│   └── service/PlatformAverageCalculator.java
│
└── config/
    ├── AsyncConfig.java
    ├── JpaConfig.java
    └── SchedulerConfig.java              ← 스케줄러 스레드풀 설정 (크기: 4)

harvester/src/main/resources/
├── application.yml                        ← Rate Limit 임계값, 스케줄 설정 등
├── application-dev.yml
├── application-local.yml
├── application-prod.yml
└── graphql/
    ├── contributed-repositories.graphql   ← 기여 저장소 조회 (월별, maxRepositories: 100)
    ├── repository-commits.graphql         ← 커밋 조회 (저장소별, 30개/페이지)
    ├── user-pull-requests.graphql         ← PR 조회 (30개/페이지)
    ├── user-issues.graphql                ← 이슈 조회 (100개/페이지)
    ├── user-basic-info.graphql            ← 유저 기본정보·소유 저장소 (100개/페이지)
    └── user-contributions.graphql         ← 기여도 캘린더
```

---

## 2. 전체 흐름 개요

### 시작 흐름

```
외부에서 RabbitMQ 큐(github-collection)에 메시지 투입
         ↓
GithubCollectionListener 메시지 수신
  ├─ 삭제된 유저? → 메시지 폐기 (ACK)
  ├─ 이미 실행 중? → 메시지 폐기 (ACK)  ← 중복 실행 방지
  └─ 정상 → PriorityJobLauncher에 전달
         ↓
Spring Batch Job 실행 (9단계 파이프라인)
         ↓
JobSchedulingListener.afterJob() 실행
  ├─ 성공 or Rate Limit → Rate Limit 리셋 시간 + 5분 후 재등록
  └─ 기타 에러 → 현재 시간 + 30분 후 재등록
         ↓
RabbitMQ 지연 메시지(x-delay 헤더)로 다음 실행 자동 예약
```

### 조직 저장소 별도 흐름

개인 크롤링과 완전히 독립된 흐름이다.

```
backend 서버에서 조직 등록 이벤트 발행
         ↓
OrgCollectionListener가 별도 큐(github-org-collection)에서 수신
         ↓
GitHub REST API로 조직 저장소 목록 조회
  → /orgs/{orgName}/repos?per_page=100&type=all&page={page}
         ↓
GithubRepositoryStatistics에 저장 (isOwner=true)
```

---

## 3. 트리거 방식

크롤링은 **RabbitMQ 메시지 기반 이벤트 드리븐**으로 동작한다.
`github-collection` 큐에 `GithubCollectionRequest`가 들어오면 `GithubCollectionListener`가 소비하여 Spring Batch Job을 실행한다.

- ACK 모드: Manual (처리 완료 후 명시적 ACK)
- 중복 실행 방지: 동일 유저의 Job이 이미 실행 중이면 메시지 폐기
- 삭제된 유저: 메시지 수신 즉시 폐기

---

## 4. 9단계 파이프라인

```
[1] RepositoryDiscoveryStep   → 기여 저장소 목록 수집 (GraphQL, 월별 청크)
[2] PullRequestMiningStep     → PR 전체 수집 (GraphQL 페이지네이션)
[3] IssueMiningStep           → 이슈 전체 수집 (GraphQL 페이지네이션)
[4] CommitMiningStep          → 저장소별 커밋 수집 (GraphQL, 저장소별 페이지네이션)
[5] StatisticsAggregationStep → MongoDB 집계 → PostgreSQL 통계 저장
[6] ScoreCalculationStep      → 활동성·다양성·영향력 점수 계산
[7] ChallengeEvaluationStep   → RabbitMQ로 챌린지 서비스에 이벤트 발행
[8] PlatformAverageStep       → 전체 평균 재계산 (10명 이상 증가 시)
[9] CleanupStep               → lastCrawling 갱신, 민감정보 정리
```

---

## 5. 각 Step 상세

### [1] RepositoryDiscoveryStep

1. GitHub 토큰 복호화
2. `CollectionMetadata`에서 마지막 수집 날짜 조회
   - 첫 수집: 계정 생성일부터
   - 이후: lastFullCollection 날짜부터 증분 수집
3. `TimeChunkGenerator`로 월별 청크 생성 후 청크마다 `contributedReposQuery` 호출 (100ms 대기)
4. `fetchOwnedRepositories()`로 소유 저장소 추가
5. ExecutionContext에 저장: `GITHUB_LOGIN`, `GITHUB_TOKEN`, `GITHUB_NODE_ID`, `DISCOVERED_REPOS`

**GraphQL 쿼리 제한:**
- `commitContributionsByRepository(maxRepositories: 100)`
- `pullRequestContributionsByRepository(maxRepositories: 100)`
- `issueContributionsByRepository(maxRepositories: 100)`
- → 한 달에 저장소 100개 초과 기여 시 101번째부터 누락

### [2] PullRequestMiningStep

- `getUserPullRequests()` 커서 기반 페이지네이션 (30개/페이지)
- MongoDB `PullRequestDocument` 저장
- 중복 체크: `userId + repositoryName + prNumber`

### [3] IssueMiningStep

- `getUserIssues()` 커서 기반 페이지네이션 (100개/페이지)
- MongoDB `IssueDocument` 저장
- 중복 체크: `userId + repositoryName + issueNumber`

### [4] CommitMiningStep

- `DISCOVERED_REPOS` 배열의 저장소별로 순회
- 기본 페이지 크기: 30 / 실패 시 재시도: 10
- MongoDB `CommitDocument` 저장
- 중복 체크: `userId + repositoryName + sha`

### [5] StatisticsAggregationStep

MongoDB에서 집계 → PostgreSQL 저장:
- 총 커밋 수, 라인 변경량(추가/삭제)
- 야간 커밋 수 (22시~6시)
- 소유/기여 저장소 수, 스타 수, 포크 수

### [6] ScoreCalculationStep

| 점수 | 기준 | 최대 |
|------|------|------|
| 활동성 | 커밋 100+, PR 20+ → 3pt / 커밋 30+, PR 5+ → 2pt / 커밋 5+ OR PR 1+ → 1pt | 3pt |
| 다양성 | 기여 저장소 10개+ → 1.0pt / 5~9개 → 0.7pt / 2~4개 → 0.4pt | 1pt |
| 영향력 | 소유 저장소 스타 100+ → +2.0 / 스타 1000+ 저장소 PR 머지 → +1.5 / 이슈 10개+ 클로즈 → +1.0 / 크로스 저장소 PR → +0.5 | 5pt |

### [7] ChallengeEvaluationStep

- `ChallengeEvaluationRequest` 생성 후 `CHALLENGE_EVALUATION` 큐에 발행
- challenge-service가 별도로 소비하여 챌린지 달성 여부 판단

### [8] PlatformAverageStep

- 현재 전체 사용자 수와 마지막 계산 시 사용자 수 차이가 10명 이상일 때만 재계산
- 매시간 정각 스케줄러(`PlatformAverageScheduler`)로도 독립 실행

### [9] CleanupStep

- `GithubUser.lastCrawling` 갱신
- `CollectionMetadata.lastFullCollection` 갱신
- ExecutionContext에서 `DISCOVERED_REPOS` 제거

---

## 6. 상황별 동작

| 상황 | 처리 방식 |
|------|----------|
| **신규 유저** | `CollectionMetadata` 없음 → GitHub 계정 생성일부터 전체 수집 |
| **기존 유저** | `lastFullCollection` 날짜부터 증분 수집 |
| **삭제된 유저** | 메시지 수신 즉시 폐기 (ACK 처리) |
| **중복 실행** | 동일 유저 Job 실행 중이면 메시지 폐기 |
| **조직 저장소** | `OrgCollectionListener`가 REST API로 독립 수집 (별도 큐) |

---

## 7. 에러 처리

### GraphQL 에러 분류 (`GraphQLErrorHandler`)

| 에러 타입 | 조건 | 동작 |
|-----------|------|------|
| `RETRYABLE` | 응답 null, 일반 네트워크 오류 | exponential backoff 3회 재시도 (2~30초) |
| `NON_RETRYABLE` | "Something went wrong" 메시지 | 즉시 저장소 스킵 |
| `PARTIAL` | 부분 에러 (data 있음) | 경고 로그 후 계속 진행 |

### 페이지네이션 중간 에러 (`PaginationHelper`)

```
첫 페이지 에러 → RETRYABLE: -1 반환 (페이지 크기 축소 재시도)
              → NON_RETRYABLE: -2 반환 (저장소 스킵)
중간 페이지 에러 → break (그때까지 저장된 데이터만 유지, 이후 페이지 영구 누락)
              ※ 다음 증분 수집 때도 복구 안 됨 — 중복 체크(SHA/PR번호 등)로 이미 저장된 걸로 판단하기 때문
```

### Rate Limit 에러

- 임계값(100 포인트 이하): `RateLimitException` 발생 → Job 중단
- Rate Limit 리셋 시간 + 5분 후 RabbitMQ에 재등록 (처음부터 재시작)
- 기타 에러: 30분 후 재시도

### Commit 재시도 전략

```
페이지 크기 30으로 시도
  ├─ 성공 → 완료
  ├─ -2 (NON_RETRYABLE) → 저장소 스킵
  └─ -1 (RETRYABLE) → 페이지 크기 10으로 재시도
       ├─ 성공 → 완료
       └─ 실패 → 저장소 스킵
```

---

## 8. 자동 순환 스케줄링

Job 완료 후 `JobSchedulingListener.afterJob()` 실행:

```
Job 성공    → Rate Limit 리셋 시간 + 5분 후 다음 수집 예약
Rate Limit  → Rate Limit 리셋 시간 + 5분 후 재시도 예약
기타 에러   → 현재 시간 + 30분 후 재시도 예약
```

RabbitMQ 지연 메시지(`x-delay` 헤더)를 사용하여 다음 실행 시점에 자동 큐 등록.
발행 실패 시 최대 3회 재시도 (1초 간격).

---

## 9. 데이터 저장소

MongoDB에 raw 데이터를 먼저 쌓고, Step 5(StatisticsAggregationStep)에서 집계해 PostgreSQL로 요약 저장하는 2단계 구조다.

```
GitHub API
    ↓ 수집 (Step 1~4)
MongoDB (raw 데이터)
    ↓ 집계 (Step 5)
PostgreSQL (요약 통계 + 점수)
```

| 저장소 | 저장 데이터 | 역할 |
|--------|------------|------|
| **MongoDB** | ContributedRepoDocument, PullRequestDocument, IssueDocument, CommitDocument, CollectionMetadataDocument | 수집 원본 보관, 수집 메타데이터 |
| **PostgreSQL** | GithubUserStatistics, GithubRepositoryStatistics, PlatformStatistics | 집계된 통계, 점수, 플랫폼 평균 |

---

## 10. 주요 설정값

| 항목 | 값 |
|------|----|
| RabbitMQ ACK 모드 | manual |
| Rate Limit 임계값 | 100 포인트 |
| GraphQL 연결 풀 | 60개 |
| GraphQL 타임아웃 | 60초 |
| GraphQL 재시도 | 3회 (exponential backoff 2~30초) |
| Commit 기본 페이지 크기 | 30 |
| Commit 재시도 페이지 크기 | 10 |
| API 요청 간 대기 | 100ms |
| 플랫폼 평균 스케줄 | 매시간 정각 |
| 플랫폼 평균 재계산 임계값 | 사용자 10명 증가 |
| 일반 에러 재시도 지연 | 30분 |
| Rate Limit 후 추가 대기 | 5분 |

---

## 11. 누락 가능 지점

### 위험도 순위

| 순위 | 원인 | 위험도 | 발생 조건 |
|------|------|--------|----------|
| 1 | `maxRepositories: 100` 한계 | 매우 높음 | 한 달 기여 저장소 100개 초과 시 101번째부터 누락 |
| 2 | 중간 페이지 에러 시 루프 종료 | 높음 | GraphQL 에러 발생 시 이후 페이지 영구 누락, 다음 증분 수집에서도 복구 안 됨 |
| 3 | Rate Limit 임계값 도달 | 중간 | 대량 저장소 사용자에서 수집 중단 → 재시작 시 처음부터 다시 시작 |
| 4 | ExecutionContext 크기 한계 | 중간 | 기여 저장소 수만 개 이상 시 직렬화 실패 가능 |
| 5 | 삭제·비공개 전환 데이터 미감지 | 낮음 | 삭제된 저장소·커밋이 MongoDB에 잔류 (증분 수집 특성상 감지 불가) |

### 핵심 코드 위치

```
# 1번: contributed-repositories.graphql L5, L29, L45
commitContributionsByRepository(maxRepositories: 100)

# 2번: PaginationHelper.java
if (result.hasError && totalSaved > 0) {
    break;  // 이후 페이지 영구 누락
}

# 3번: RateLimitManager.java
if (remaining <= threshold) {  // threshold = 100
    throw new RateLimitException(...);
}
```

---

## 12. Rate Limit (OAuth 토큰 5000포인트)

### 사실 여부

GitHub OAuth 인증 토큰 기준:
- **GraphQL API**: 시간당 **5000 포인트** (복잡도 기반)
- **REST API**: 시간당 **5000 요청**

K-OSP는 GraphQL 중심(약 95%)으로 동작하므로 GraphQL 포인트가 핵심 지표.

### 쿼리별 포인트 소모

| 쿼리 | 포인트/호출 |
|------|------------|
| `contributed-repositories.graphql` | 4~5 |
| `user-basic-info.graphql` | 2~3 |
| `repository-commits.graphql` | 1 |
| `user-pull-requests.graphql` | 1 |
| `user-issues.graphql` | 1 |

### 1회 수집 시뮬레이션 (저장소 50개 / 커밋 500개 / PR 100개 / 이슈 100개 / 계정 5년)

| 단계 | 호출 횟수 | 소모 포인트 |
|------|----------|------------|
| 월별 기여 저장소 조회 (60개월) | 60회 | 240~300 |
| 소유 저장소 조회 | 1회 | 2 |
| 저장소별 커밋 수집 (50개) | 50회 | 50 |
| PR 수집 (100개 ÷ 30) | 4회 | 4 |
| 이슈 수집 (100개 ÷ 100) | 1회 | 1 |
| **합계** | **116회** | **297~357 포인트** |

→ 5000포인트 중 약 **6~7% 사용** — 일반 개발자 수준에서 Rate Limit 문제 없음

### 5000포인트로 수집 가능한 최대 규모

고정 비용(월별 저장소 조회) 약 300포인트 제외, 남은 4700포인트 기준:

| 항목 | 최대 수집량 |
|------|------------|
| 커밋 (30개/포인트) | **141,000개** |
| PR (30개/포인트) | **141,000개** |
| 이슈 (100개/포인트) | **470,000개** |

### Rate Limit 문제 발생 임계점

| 규모 | 예상 소모 포인트 | 결과 |
|------|----------------|------|
| 저장소 1,000개 + 커밋 10,000개 | ~1,500pt | 정상 |
| 저장소 3,000개 + 커밋 50,000개 | ~3,500pt | 주의 |
| **저장소 5,000개 + 커밋 100,000개** | **~6,000pt** | **한도 초과 → 중단 후 재시도** |

→ **저장소 3,000개 이상 또는 커밋 5만 개 이상**인 극단적 사용자에서 1회 수집이 1시간을 넘길 수 있음

### 현재 임계값 (100포인트) 평가

현재 코드는 남은 포인트가 100 이하이면 수집을 중단함.
실제 일반 유저 기준 쿼리 1회에 1~5포인트 소모이므로 **임계값을 50 이하로 낮춰도 안전함**.
보수적으로 100으로 설정되어 있어 불필요하게 일찍 중단될 수 있음.

---

## 13. 왜 큐(RabbitMQ)를 사용하는가

단순한 스케줄러(`@Scheduled`)가 아닌 메시지 큐를 쓰는 이유는 3가지다.

| 문제 | 큐 없이 | 큐 사용 |
|------|---------|---------|
| 유저별 독립 실행 | 직접 스케줄 관리 복잡 | 메시지 하나 = 작업 하나 |
| Rate Limit 대기 | `Thread.sleep()` → 스레드 블로킹·자원 낭비 | `x-delay` 헤더로 스레드 즉시 해방 |
| 완료 후 재예약 | 별도 타이머 서버 필요 | 완료 시점에 메시지 재발행으로 해결 |

**핵심은 Rate Limit 대기 방식이다.**

GitHub API 한도에 걸리면 리셋까지 최대 1시간을 기다려야 한다. 코드 안에서 sleep으로 막으면 그 시간 동안 스레드가 아무것도 못 하는 상태로 잠긴다. RabbitMQ의 지연 메시지를 쓰면 "N분 후에 이 메시지를 다시 투입해줘"로 해결되고, 스레드는 즉시 해방되어 다른 유저 작업을 처리할 수 있다.

```
Rate Limit 도달
    ↓
x-delay = (리셋시간 + 5분) 으로 메시지 재발행 후 즉시 반환
    ↓
스레드 해방 → 다른 유저 작업 가능
    ↓
지연 시간이 지나면 큐가 자동으로 메시지 재투입
```

---

## 14. 항목별 수집 필드 상세

크롤링은 단순 개수가 아니라 **각 항목의 내부 필드까지 읽는다.**

### 저장소

기여 저장소(`contributed-repositories.graphql`)와 소유 저장소(`user-basic-info.graphql`) 두 경로로 수집.

| 필드 | 용도 |
|------|------|
| `nameWithOwner` | 저장소 식별 |
| `stargazerCount` | 영향력 점수 계산 |
| `forkCount` | 통계 |
| `primaryLanguage` | 통계 |
| `isPrivate` / `isFork` | 구분 |
| `owner.login` | 소유자 판단 (`login == owner.login` → isOwner) |

### 커밋

`author: {id: $authorId}` 필터로 해당 유저 커밋만 수집. 봇·merge 커밋 별도 필터 없음.

| 필드 | 용도 |
|------|------|
| `oid` (SHA) | 중복 체크 기준 |
| `message` | 저장 |
| `additions` / `deletions` | 라인 변경량 통계 |
| `changedFilesIfAvailable` | 변경 파일 수 |
| `authoredDate` | 야간 커밋 판단 (22시~6시) |
| `author.name` / `email` | 저장 |

### PR

| 필드 | 용도 |
|------|------|
| `number` | 중복 체크 기준 |
| `state` | OPEN / CLOSED / MERGED |
| `merged` / `mergedAt` | 병합 여부 |
| `additions` / `deletions` / `changedFiles` | 변경량 |
| `isCrossRepository` | 크로스 저장소 PR 여부 → 영향력 +0.5pt |
| `closingIssuesReferences.totalCount` | 이 PR로 닫힌 이슈 수 → 10개+ 시 영향력 +1pt |
| `repository.stargazerCount` | 기여 저장소 스타 수 → 1000+ 시 영향력 +1.5pt |

### 스타

별도 스타 수집 API 없음. 두 경로로 간접 수집.

| 경로 | 수집 위치 | 용도 |
|------|----------|------|
| 내 소유 저장소 스타 | `user-basic-info.graphql → stargazerCount` | 100+ → 영향력 +2pt |
| PR 머지한 저장소 스타 | `user-pull-requests.graphql → repository.stargazerCount` | 1000+ → 영향력 +1.5pt |

→ "내가 준 스타 목록"이나 "받은 스타 목록"을 수집하는 게 아니라, **점수 계산에 필요한 저장소별 스타 수치**만 읽는다.

---

## 15. 프로필 페이지 표시 기준

`/user/{id}` 페이지에 보이는 각 수치의 실제 출처.

| 표시 항목 | API 엔드포인트 | 출처 | 기준 |
|----------|--------------|------|------|
| Commits | `/v1/users/{id}/github/overall-history` | `GithubUserStatistics.totalCommits` | 크롤링된 전체 누적 합산 |
| Pull Requests | 동일 | `GithubUserStatistics.totalPrs` | 전체 누적 합산 |
| Issues | 동일 | `GithubUserStatistics.totalIssues` | 전체 누적 합산 |
| Recent Repos | `/v1/users/{id}/github/recent-activity` | `GithubRepositoryStatistics` | 최근 커밋 기준 **최대 10개** |

### Recent Repos가 10개로 고정되는 이유

```java
// GithubStatisticsService.java
private static final Integer RECENT_ACTIVITY_LIMIT = 10;

repositoryStatisticsRepository
    .findTopNByContributorGithubIdOrderByLastCommitDateDesc(githubId, RECENT_ACTIVITY_LIMIT);
```

프론트에서 이 목록의 길이를 개수로 표시하기 때문에, 저장소가 10개 이상이면 항상 **"10"** 으로 고정된다.

```tsx
// UserProfileClient.tsx
const recentRepositoryCount = recentActivity.length;  // 최대 10
```

**10개로 제한한 이유:**
- 프로필에서 보여줄 건 전체 기록이 아닌 최근 활동이라는 설계 방향
- 저장소별 커밋 수·PR 수·스타 수까지 표시하는 구조라 전체 나열 시 UI가 너무 길어짐
- 성능: 수백 개 저장소를 매 프로필 조회마다 불러오는 부담

**현재 문제:** `overallHistory.contributedRepoCount`(실제 전체 기여 저장소 수)가 응답에 있지만 카드 수치로 사용되지 않고 있어, Recent Repos 숫자가 전체 저장소 수처럼 오해될 수 있음.
