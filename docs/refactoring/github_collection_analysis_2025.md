# GitHub 데이터 수집 시스템 분석 및 개선안 (2025.01)

## 1. 개요 (Executive Summary)

이 문서는 KOSP 백엔드의 GitHub 데이터 수집 시스템 현재 상태를 **실제 코드 기준**으로 분석하고, 기존 문서(`github_collection_refactoring_plan.md`)에서 제시된 문제점과의 차이를 비교하며, 새로운 개선 방향을 제시합니다.

### 핵심 발견 사항

| 항목 | 기존 문서의 가정 | 실제 코드 상태 | 평가 |
|------|-----------------|---------------|------|
| **Rate Limit 처리** | Thread.sleep() 블로킹 | `RateLimitException` + 재스케줄링 | **✅ 이미 개선됨** |
| **Batch vs Queue 이원화** | 두 시스템이 병행 | Redis Queue가 주요 경로, Batch는 보조 | **⚠️ 부분적 해결** |
| **증분 수집** | 미구현 | `since` 파라미터 사용 (commits) | **✅ 부분 구현됨** |
| **작업 세분화** | 통짜 로직 | Job Type별 분리 (5종류) | **✅ 이미 개선됨** |
| **책임 분리** | SRP 위반 | 여전히 Service 클래스에 책임 집중 | **❌ 미해결** |

---

## 2. 현재 시스템 구조 분석 (As-Is Architecture)

### 2.1. 데이터 흐름 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              TRIGGER LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│  UserSignupEvent ─────┐                                                          │
│                       ▼                                                          │
│            GithubDataCollectionEventListener                                     │
│                       │                                                          │
│                       ▼                                                          │
│            GithubDataCollectionRetryService.collectWithRetry()                   │
│                       │                                                          │
│          ┌────────────┼────────────┐                                             │
│          ▼            ▼            ▼                                             │
│   enqueueUserCollection   getRepositoryList   enqueueRepositoryCollection        │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                               QUEUE LAYER (Redis)                                │
├─────────────────────────────────────────────────────────────────────────────────┤
│  CollectionJobProducer                                                           │
│    ├── trackUserJobs(count) → Redis counter                                      │
│    └── enqueue(CollectionJob) → Redis ZSet (priority_queue)                      │
│                                                                                  │
│  CollectionJob Types:                                                            │
│    ├── USER_BASIC (priority=1)                                                   │
│    ├── USER_EVENTS (priority=2)                                                  │
│    ├── REPO_ISSUES (priority=3)                                                  │
│    ├── REPO_PRS (priority=3)                                                     │
│    └── REPO_COMMITS (priority=4)                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              WORKER LAYER                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│  GithubCollectionWorker (@Scheduled, @Async)                                     │
│    ├── pollJob() → Redis ZPOPMIN (atomic claim)                                  │
│    ├── processJob(job) → switch by type                                          │
│    │     ├── USER_BASIC → collectUserBasicInfo()                                 │
│    │     ├── USER_EVENTS → collectUserEvents()                                   │
│    │     ├── REPO_ISSUES → collectIssues()                                       │
│    │     ├── REPO_PRS → collectPullRequests()                                    │
│    │     └── REPO_COMMITS → collectAllCommits()                                  │
│    └── handleFailure() → exponential backoff + retry                             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           DATA COLLECTION LAYER                                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│  GithubDataCollectionService                                                     │
│    ├── collectUserBasicInfo() → GraphQL API                                      │
│    ├── collectUserEvents() → REST /users/{id}/events                             │
│    ├── collectIssues() → REST /repos/{owner}/{repo}/issues                       │
│    ├── collectPullRequests() → REST /repos/{owner}/{repo}/pulls                  │
│    └── collectCommitDetail() → REST /repos/{owner}/{repo}/commits/{sha}          │
│                                                                                  │
│  GithubCommitCollectionService                                                   │
│    └── collectAllCommits() → REST /repos/{owner}/{repo}/commits?author={login}   │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              STORAGE LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│  MongoDB (Raw Data)                           MySQL (Statistics)                 │
│    ├── GithubUserBasicRaw                      ├── GithubUserStatistics          │
│    ├── GithubUserEventsRaw                     ├── GithubGlobalStatistics        │
│    ├── GithubIssueRaw                          ├── GithubYearlyStatistics        │
│    ├── GithubPRRaw                             ├── GithubMonthlyStatistics       │
│    ├── GithubCommitDetailRaw                   └── GithubRepositoryStatistics    │
│    └── GithubCollectionMetadata                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            COMPLETION LAYER                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│  CollectionCompletionTracker                                                     │
│    ├── decrementJobCount() → Redis counter                                       │
│    └── if count == 0 → publish events:                                           │
│          ├── UserStatisticsCalculationRequestedEvent                             │
│          └── GlobalStatisticsCalculationRequestedEvent                           │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2. 핵심 컴포넌트 역할

| 컴포넌트 | 파일 위치 | 핵심 역할 | LOC |
|----------|-----------|----------|-----|
| **GithubDataCollectionService** | `/service/` | API 호출 + MongoDB 저장 | 444 |
| **GithubDataCollectionRetryService** | `/service/` | 트리거 진입점 + Job 등록 | 267 |
| **GithubCommitCollectionService** | `/service/` | Commit 전용 수집 (author 필터링) | 136 |
| **GithubCollectionWorker** | `/queue/service/` | Job 폴링 + 실행 + 에러 처리 | 270 |
| **CollectionJobProducer** | `/queue/service/` | Job 생성 + Redis 등록 | 168 |
| **CollectionCompletionTracker** | `/queue/service/` | 완료 감지 + 통계 이벤트 발행 | 153 |
| **RateLimitManager** | `/client/rest/` | Rate Limit 추적 + 예외 발생 | 106 |
| **GithubRestApiClient** | `/client/rest/` | REST API 호출 + 페이지네이션 | 256 |
| **GithubGraphQLClient** | `/client/graphql/` | GraphQL API 호출 | 145 |
| **FailureAnalyzer** | `/service/` | 에러 분류 + 재시도 판단 | 192 |

---

## 3. 기존 문서 대비 현재 상태 비교

### 3.1. 이미 해결된 문제 (Resolved Issues)

#### A. Rate Limit 처리 - ✅ Non-Blocking 방식 구현됨

**기존 문서 (Section 2.5)**:
> Rate Limit 발생 시 `Thread.sleep()`으로 전체 워커 스레드를 차단합니다.

**실제 코드 (RateLimitManager.java:41-44)**:
```java
// ❌ Mono.delay()로 블로킹하지 않음!
// ✅ 예외를 던져서 Worker가 작업을 재스케줄하도록 함
return Mono.error(new RateLimitException(
    "Rate limit threshold reached. Remaining: " + remaining,
    waitTime
));
```

**Worker 처리 (GithubCollectionWorker.java:73-84)**:
```java
} catch (rest.client.github.domain.io.swkoreatech.kosp.RateLimitException e) {
    // ✅ Rate Limit 도달 - 작업을 재스케줄 (스레드 블로킹 없음!)
    if (job != null) {
        long waitMillis = e.getWaitTime().toMillis();
        log.warn("⚠️ Rate limit reached for job {}. Rescheduling after {} ms", 
            job.getJobId(), waitMillis);
        
        redisTemplate.opsForHash().delete(PROCESSING_KEY, job.getJobId());
        jobProducer.enqueueWithDelay(job, waitMillis);
    }
    // ❌ 예외를 다시 던지지 않음 - Worker는 즉시 다음 작업 처리
```

**평가**: Non-blocking backoff가 이미 구현되어 있음. 기존 문서의 문제점은 해결됨.

---

#### B. 작업 세분화 - ✅ Job Type별 분리됨

**기존 문서 (Section 2.4)**:
> 커밋이 10만 개 이상인 레포지토리 수집 시, 단일 API 호출 내에서 전체를 처리하려고 합니다.

**실제 코드 (CollectionJobType.java)**:
```java
public enum CollectionJobType {
    USER_BASIC,      // 사용자 기본 정보 수집
    USER_EVENTS,     // 사용자 이벤트 수집
    REPO_ISSUES,     // 레포지토리 이슈 수집
    REPO_PRS,        // 레포지토리 PR 수집
    REPO_COMMITS,    // 레포지토리 커밋 수집
    TIMELINE         // 사용자 Timeline 스크래핑
}
```

**평가**: 5가지 Job Type으로 이미 분리됨. 다만 페이지 단위 분리는 미구현.

---

#### C. Author 필터링 - ✅ 구현됨 (대용량 레포 타임아웃 해결)

**실제 코드 (GithubCommitCollectionService.java:110-112)**:
```java
// ✅ API 레벨 필터링: author 쿼리 파라미터 사용
String uri = String.format("/repos/%s/%s/commits?author=%s", repoOwner, repoName, githubLogin);

log.info("Fetching commits for {}/{} authored by {}", repoOwner, repoName, githubLogin);
```

**평가**: 대용량 레포지토리에서 특정 사용자의 커밋만 가져오므로 타임아웃 문제 완화됨.

---

### 3.2. 부분적으로 해결된 문제 (Partially Resolved)

#### A. Batch vs Queue 이원화 - ⚠️ Queue가 주요 경로가 됨

**기존 문서 (Section 2.3)**:
> 회원가입 시에는 Redis Queue(`collectWithRetry`), 정기 스케줄링 시에는 Spring Batch(`collectAllData`)를 사용합니다.

**실제 코드 분석**:
- `GithubDataCollectionEventListener` → `collectWithRetry()` → Redis Queue ✅
- `BatchScheduler` → 파일이 존재하지만 `/batch/` 디렉토리 내 실제 구현 확인 필요
- `collectAllData()` 메서드가 `GithubDataCollectionRetryService`에 존재하지만 **Redis Queue 사용 없이 동기식 실행**

```java
// GithubDataCollectionRetryService.java:100-113
public void collectAllData(String githubLogin, String token) {
    collectUserData(githubLogin, token);
    collectUserEvents(githubLogin, token);
    
    List<Map<String, Object>> repositories = getRepositoryList(githubLogin, token);
    
    for (Map<String, Object> repository : repositories) {
        collectRepositoryData(repository, token);  // 동기식!
    }
    
    log.info("Completed all data collection for user: {}", githubLogin);
}
```

**평가**: 
- 회원가입 경로: Redis Queue 사용 ✅
- 배치 경로: 동기식 `collectAllData()` 존재 (코드 중복)
- **두 시스템 병존** - 기존 문서의 문제점이 일부 남아있음

---

#### B. 증분 수집 - ⚠️ Commit만 구현됨

**기존 문서 (Section 2.1)**:
> `state=all` 파라미터를 사용하여 **전체 히스토리**를 매번 조회

**실제 코드 분석**:

**Commit - 증분 수집 구현됨** (GithubDataCollectionRetryService.java:171-219):
```java
// 메타데이터 확인하여 증분 수집 여부 결정
GithubCollectionMetadata metadata = metadataRepository
    .findByRepoOwnerAndRepoNameAndCollectionType(repoOwner, repoName, "commits")
    .block();

if (metadata != null) {
    // 증분 수집
    return getCommitsSince(repoOwner, repoName, token, metadata);
} else {
    // 첫 수집
    return getAllCommitsFirstTime(repoOwner, repoName, token);
}
```

**Issues/PRs - 전체 히스토리 수집** (GithubDataCollectionService.java:203-204, 240-241):
```java
// Issues
String uri = String.format("/repos/%s/%s/issues?state=all", repoOwner, repoName);

// PRs
String uri = String.format("/repos/%s/%s/pulls?state=all", repoOwner, repoName);
```

**평가**: 
- Commits: `since` 파라미터 사용 ✅
- Issues/PRs: `state=all`로 전체 수집 ❌
- **50% 해결됨**

---

### 3.3. 미해결 문제 (Unresolved Issues)

#### A. 책임의 혼재 (SRP 위반) - ❌ 여전히 문제

**기존 문서 (Section 2.2)**:
> `GithubDataCollectionService`가 API 호출, 데이터 필터링, DB 저장, 이벤트 발행을 모두 담당

**실제 코드 분석 (GithubDataCollectionService.java)**:

| 메서드 | 역할 | 책임 수 |
|--------|------|---------|
| `collectUserBasicInfo()` | API 호출 + 데이터 변환 + MongoDB 저장 | 3 |
| `collectIssues()` | API 호출 + 데이터 변환 + MongoDB 저장 | 3 |
| `collectPullRequests()` | API 호출 + 데이터 변환 + MongoDB 저장 | 3 |
| `collectCommitDetail()` | API 호출 + 중복 체크 + 데이터 변환 + MongoDB 저장 | 4 |

**예시 코드** (GithubDataCollectionService.java:142-193):
```java
public Mono<GithubCommitDetailRaw> collectCommitDetail(...) {
    // 1. 중복 체크 (Validation)
    if (commitDetailRawRepository.existsBySha(sha)) {
        return Mono.empty();
    }
    
    // 2. API 호출 (External Call)
    return restApiClient.get(uri, token, Map.class)
        // 3. 데이터 변환 (Transformation)
        .map(response -> {
            // ... parsing logic ...
            GithubCommitDetailRaw raw = GithubCommitDetailRaw.create(...);
            // 4. DB 저장 (Persistence)
            return commitDetailRawRepository.save(raw);
        });
}
```

**평가**: 
- API 호출, 데이터 변환, 저장이 하나의 메서드에 혼재
- 테스트 작성이 어려움 (외부 의존성 모킹 필요)
- **Reader-Processor-Writer 분리 필요**

---

#### B. 페이지 단위 Job 분리 - ❌ 미구현

**기존 문서 (Section 4.4)**:
> "외부로 1회 호출 = Job 1개"

**실제 코드 (GithubRestApiClient.java:206-230)**:
```java
private <T> Mono<Void> collectAllPages(
    String uri,
    String token,
    Class<T> itemType,
    int page,
    List<T> accumulator
) {
    String paginatedUri = buildPaginatedUri(uri, page);
    
    return get(paginatedUri, token, List.class)
        .flatMap(items -> {
            // ... accumulate items ...
            if (items.size() < 100) {
                return Mono.empty();
            }
            // 재귀 호출 (동일 Job 내에서!)
            return collectAllPages(uri, token, itemType, page + 1, accumulator);
        });
}
```

**문제점**:
- 페이지네이션이 하나의 Job 내에서 재귀적으로 처리됨
- 중간에 실패하면 전체 페이지 재수집 필요
- 대용량 데이터에서 메모리 누적

**평가**: 기존 문서에서 제안한 "1 Call = 1 Job" 패턴 미적용

---

#### C. 1시간 중복 필터링의 한계 - ⚠️ 새로 발견된 문제

**실제 코드 (GithubDataCollectionRetryService.java:63-72)**:
```java
GithubCollectionMetadata metadata = metadataRepository
    .findByRepoOwnerAndRepoNameAndCollectionType(repoOwner, repoName, "commits")
    .block();

if (metadata != null && metadata.getLastCollectedAt().isAfter(
    java.time.LocalDateTime.now().minusHours(1))) {
    log.debug("Skipping repository {}/{} as it was collected recently", ...);
} else {
    jobProducer.enqueueRepositoryCollection(...);
}
```

**문제점**:
- 1시간 이내 재수집 방지는 좋으나, **커밋만 체크**함 (`"commits"`)
- Issues/PRs의 메타데이터는 별도 관리 안됨
- 부분 실패 시 재수집 판단 어려움

---

#### D. 동시 수집 충돌 방지 미비 - ⚠️ 새로 발견된 문제

**실제 코드 (CollectionJobProducer.java:116-127)**:
```java
public void enqueue(CollectionJob job) {
    enqueueWithDelay(job, 0);
}

public void enqueueWithDelay(CollectionJob job, long delayMillis) {
    job.setJobId(UUID.randomUUID().toString());  // 항상 새 ID 생성!
    job.setCreatedAt(LocalDateTime.now());
    job.setRetryCount(0);
    // ... Redis 등록 ...
}
```

**문제점**:
- 동일한 (User, Type, Repo) 조합의 Job이 이미 있는지 체크 안함
- 회원가입 + 스케줄러 동시 실행 시 중복 Job 생성 가능
- 기존 문서 Section 4.7.7에서 제안한 Deduplication 미구현

---

## 4. 새로운 아키텍처 제안 (To-Be Architecture)

### 4.1. 핵심 개선 방향

| 우선순위 | 문제 | 개선 방향 | 예상 효과 |
|----------|------|----------|----------|
| **P0** | SRP 위반 | Reader-Processor-Writer 패턴 적용 | 테스트 용이성, 유지보수성 향상 |
| **P0** | Issues/PRs 전체 수집 | `since` 파라미터 적용 | API Quota 50%+ 절감 |
| **P1** | 페이지 단위 미분리 | 1 API Call = 1 Job | 실패 복구 시간 단축 |
| **P1** | 중복 Job 방지 미비 | Deduplication 체크 추가 | 불필요한 재수집 방지 |
| **P2** | Batch/Queue 이원화 | 단일 이벤트 기반 통합 | 코드 중복 제거 |

### 4.2. 목표 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          UNIFIED TRIGGER LAYER                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  UserSignupEvent ─────┐        ScheduledJob ─────┐                               │
│                       ▼                          ▼                               │
│            ┌──────────────────────────────────────────────┐                      │
│            │     DataCollectionRequestEvent (userId)      │                      │
│            └──────────────────────────────────────────────┘                      │
│                                    │                                             │
│                                    ▼                                             │
│            ┌──────────────────────────────────────────────┐                      │
│            │     UnifiedCollectionEventListener           │                      │
│            │       └── Deduplication Check                │                      │
│            │       └── Job Creation (if not exists)       │                      │
│            └──────────────────────────────────────────────┘                      │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           JOB QUEUE LAYER (Redis)                                │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │  CollectionJob (Enhanced)                                                │    │
│  │    ├── jobId: UUID                                                       │    │
│  │    ├── type: JobType                                                     │    │
│  │    ├── userId: Long (KOSP User ID)                                       │    │
│  │    ├── githubLogin: String                                               │    │
│  │    ├── repoOwner, repoName: String (nullable)                            │    │
│  │    ├── cursor: String (pagination state)      ← NEW                      │    │
│  │    ├── since: LocalDateTime (incremental)     ← NEW                      │    │
│  │    └── deduplicationKey: String               ← NEW                      │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
│  Deduplication Key = hash(userId, type, repoOwner, repoName, cursor)             │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          WORKER LAYER (Separated)                                │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌───────────────────┐   ┌───────────────────┐   ┌───────────────────┐          │
│  │  GithubApiReader  │ → │  DataProcessor    │ → │  MongoWriter      │          │
│  │  (API Call Only)  │   │  (Transform Only) │   │  (Save Only)      │          │
│  └───────────────────┘   └───────────────────┘   └───────────────────┘          │
│                                                                                  │
│  Strategy Pattern per Job Type:                                                  │
│    ├── UserBasicFetchStrategy                                                    │
│    ├── UserEventsFetchStrategy                                                   │
│    ├── RepoIssuesFetchStrategy (with since)                                      │
│    ├── RepoPRsFetchStrategy (with since)                                         │
│    └── RepoCommitsFetchStrategy (with since + cursor)                            │
│                                                                                  │
│  Page-Level Job Creation:                                                        │
│    └── If hasNextPage → enqueue NEW Job with cursor                              │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 4.3. 주요 변경 사항

#### A. Reader-Processor-Writer 분리

```java
// BEFORE (현재)
public class GithubDataCollectionService {
    public Mono<GithubIssueRaw> collectIssues(String owner, String repo, String token) {
        // API 호출 + 변환 + 저장이 하나의 메서드에
    }
}

// AFTER (목표)
public interface GithubApiReader<T> {
    Mono<ApiResponse<T>> fetch(FetchRequest request);
}

public interface DataProcessor<I, O> {
    O process(I input);
}

public interface DataWriter<T> {
    void write(T data);
}

// Strategy 구현 예시
public class RepoIssuesFetchStrategy implements CollectionStrategy {
    private final GithubApiReader<List<IssueDto>> reader;
    private final DataProcessor<IssueDto, GithubIssueRaw> processor;
    private final DataWriter<GithubIssueRaw> writer;
    
    @Override
    public CollectionResult execute(CollectionJob job) {
        // 1. Read
        var response = reader.fetch(buildRequest(job)).block();
        
        // 2. Process
        var entities = response.getData().stream()
            .map(processor::process)
            .toList();
        
        // 3. Write
        writer.writeAll(entities);
        
        // 4. Create next page job if needed
        if (response.hasNextPage()) {
            return CollectionResult.continueWith(
                job.withCursor(response.getNextCursor())
            );
        }
        
        return CollectionResult.done();
    }
}
```

#### B. 증분 수집 (Issues/PRs)

```java
// BEFORE
String uri = String.format("/repos/%s/%s/issues?state=all", repoOwner, repoName);

// AFTER
public String buildIssuesUri(String owner, String repo, LocalDateTime since) {
    StringBuilder uri = new StringBuilder()
        .append("/repos/").append(owner).append("/").append(repo)
        .append("/issues?state=all&sort=created&direction=asc");
    
    if (since != null) {
        uri.append("&since=").append(since.format(DateTimeFormatter.ISO_DATE_TIME));
    }
    
    return uri.toString();
}
```

#### C. 페이지 단위 Job 분리

```java
// BEFORE (재귀 호출)
private <T> Mono<Void> collectAllPages(String uri, String token, int page, List<T> acc) {
    return get(buildPaginatedUri(uri, page), token, List.class)
        .flatMap(items -> {
            acc.addAll(items);
            if (items.size() < 100) return Mono.empty();
            return collectAllPages(uri, token, page + 1, acc);  // 재귀!
        });
}

// AFTER (Job 분리)
public CollectionResult execute(CollectionJob job) {
    var response = reader.fetch(buildRequest(job, job.getCursor())).block();
    
    writer.writeAll(process(response.getData()));
    
    if (response.hasNextPage()) {
        // 새로운 Job으로 분리!
        var nextJob = job.toBuilder()
            .cursor(response.getNextCursor())
            .build();
        jobProducer.enqueue(nextJob);
        
        return CollectionResult.pageComplete();
    }
    
    return CollectionResult.done();
}
```

#### D. Deduplication

```java
public class CollectionJobProducer {
    
    private static final String DEDUP_KEY_PREFIX = "github:collection:dedup:";
    
    public boolean enqueue(CollectionJob job) {
        String dedupKey = buildDeduplicationKey(job);
        
        // Redis SETNX로 중복 체크
        Boolean isNew = stringRedisTemplate.opsForValue()
            .setIfAbsent(DEDUP_KEY_PREFIX + dedupKey, "1", Duration.ofHours(1));
        
        if (Boolean.FALSE.equals(isNew)) {
            log.debug("Duplicate job detected, skipping: {}", dedupKey);
            return false;
        }
        
        // Job 등록
        redisTemplate.opsForZSet().add(PRIORITY_QUEUE_KEY, job, job.getScheduledAt());
        return true;
    }
    
    private String buildDeduplicationKey(CollectionJob job) {
        return String.format("%s:%s:%s:%s:%s",
            job.getUserId(),
            job.getType(),
            job.getRepoOwner() != null ? job.getRepoOwner() : "_",
            job.getRepoName() != null ? job.getRepoName() : "_",
            job.getCursor() != null ? job.getCursor() : "_"
        );
    }
}
```

---

## 5. 구현 로드맵 (Implementation Roadmap)

### Phase 1: 증분 수집 확대 (1주)

| Task | 파일 | 변경 내용 |
|------|------|----------|
| 1.1 | `GithubDataCollectionService` | Issues/PRs에 `since` 파라미터 적용 |
| 1.2 | `GithubCollectionMetadata` | Issues/PRs 메타데이터 확장 |
| 1.3 | 테스트 | 증분 수집 단위 테스트 추가 |

### Phase 2: Deduplication 추가 (0.5주)

| Task | 파일 | 변경 내용 |
|------|------|----------|
| 2.1 | `CollectionJobProducer` | Deduplication 체크 로직 추가 |
| 2.2 | `CollectionJob` | `deduplicationKey` 필드 추가 |

### Phase 3: Reader-Processor-Writer 분리 (2주)

| Task | 파일 | 변경 내용 |
|------|------|----------|
| 3.1 | 신규 | `GithubApiReader` 인터페이스 생성 |
| 3.2 | 신규 | `DataProcessor` 인터페이스 생성 |
| 3.3 | 신규 | `DataWriter` 인터페이스 생성 |
| 3.4 | 신규 | 각 Job Type별 Strategy 클래스 생성 |
| 3.5 | `GithubCollectionWorker` | Strategy 패턴 적용 |

### Phase 4: 페이지 단위 Job 분리 (1주)

| Task | 파일 | 변경 내용 |
|------|------|----------|
| 4.1 | `CollectionJob` | `cursor` 필드 추가 |
| 4.2 | 각 Strategy | 페이지 완료 시 새 Job 생성 로직 |
| 4.3 | `GithubRestApiClient` | 재귀 호출 제거 |

### Phase 5: Batch 통합 (1주)

| Task | 파일 | 변경 내용 |
|------|------|----------|
| 5.1 | 신규 | `DataCollectionRequestEvent` 생성 |
| 5.2 | 신규 | `UnifiedCollectionEventListener` 생성 |
| 5.3 | `BatchScheduler` | 이벤트 발행으로 변경 |
| 5.4 | 레거시 | `collectAllData()` 제거 |

---

## 6. 결론 및 권장 사항

### 6.1. 즉시 실행 가능한 개선 (Quick Wins)

1. **Issues/PRs에 `since` 파라미터 추가** - API Quota 50%+ 절감 예상
2. **Deduplication 체크 추가** - 중복 수집 방지

### 6.2. 중기 개선 (1-2개월)

1. **Reader-Processor-Writer 분리** - 테스트 용이성 확보
2. **페이지 단위 Job 분리** - 실패 복구 시간 단축

### 6.3. 장기 개선 (2-3개월)

1. **Batch/Queue 통합** - 코드 중복 제거
2. **DB Queue로 전환** (기존 문서 제안) - Redis 의존성 감소

### 6.4. 기존 문서와의 차이점

| 항목 | 기존 문서 제안 | 이 문서의 수정 |
|------|---------------|---------------|
| Rate Limit | Non-blocking 필요 | **이미 구현됨** - 우선순위 낮춤 |
| Job 세분화 | 1 Call = 1 Job | **부분 구현됨** - 페이지 단위만 추가 |
| 증분 수집 | 전체 적용 필요 | **Commit만 됨** - Issues/PRs 추가 |
| DB Queue | Redis → MySQL | **Redis 유지** - 복잡도 대비 이점 적음 |
| SRP 분리 | 언급됨 | **구체적 설계 추가** - Strategy 패턴 |

---

*문서 작성일: 2025-01-16*
*작성자: AI Assistant*
*검토 필요: 실제 Batch 스케줄러 코드 확인, 성능 테스트 결과*
