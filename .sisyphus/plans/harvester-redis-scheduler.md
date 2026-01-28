# Harvester Redis Scheduler Refactoring

## Context

### Original Request
Harvester의 GitHub 데이터 수집 Job 스케줄링을 Redis Sorted Set 기반으로 리팩토링. 테이블 기반 트리거 큐를 제거하고, Redis를 유일한 작업 큐로 사용.

### Interview Summary
**Key Discussions**:
- 인메모리 큐는 앱 재시작 시 유실됨 → Redis로 영속화
- Spring Batch 재시작 기능 활용 → 동일 runId로 실패한 Step부터 이어서
- `collection_trigger` 테이블 제거 → Redis Sorted Set으로 대체
- 탈퇴 사용자는 Job 시작 전 체크 후 스킵

**Research Findings**:
- `RateLimitManager.getResetTime()` → Rate Limit 리셋 시간 조회 가능
- `RateLimitException` → Rate Limit 에러 구분 가능
- `JobExplorer.findRunningJobExecutions()` → 중복 실행 방지 가능

---

## Work Objectives

### Core Objective
Redis Sorted Set을 유일한 작업 큐로 사용하여 Job 스케줄링을 영속화하고, Spring Batch 재시작 기능을 활용하여 실패한 Job을 이어서 실행한다.

### Concrete Deliverables
- Redis 기반 Job 큐 (`job:queue` Sorted Set)
- 수정된 `CollectionTriggerListener` (Redis polling)
- 수정된 `PriorityJobLauncher` (인메모리 큐 제거)
- 수정된 `JobSchedulingListener` (완료/실패 처리 + 다음 스케줄)
- 수정된 `UserSignupEventListener` (Redis에 ZADD)
- 삭제된 `collection_trigger` 테이블 및 관련 코드

### Definition of Done
- [x] 신규 가입 시 Redis에 즉시 실행 작업 추가됨
- [x] Harvester가 Redis에서 작업을 가져와 실행함
- [x] 성공 시 다음 스케줄 (resetTime + 5min, 새 UUID)
- [x] Rate Limit 실패 시 재시도 (resetTime + 5min, 동일 UUID)
- [x] 기타 에러 시 재시도 (now + 30min, 동일 UUID)
- [x] 탈퇴 사용자 스킵됨
- [x] 앱 재시작 후에도 큐 유지됨

### Must Have
- Redis Sorted Set 기반 우선순위 큐
- Spring Batch 재시작 (동일 runId = 동일 JobInstance)
- 탈퇴 사용자 체크
- Rate Limit 에러 구분

### Must NOT Have (Guardrails)
- `collection_trigger` 테이블 사용
- 인메모리 큐 (앱 재시작 시 유실)
- Batch 메타데이터 테이블 직접 수정
- 불필요한 추상화 레이어

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES (backend에 테스트 있음)
- **User wants tests**: NO (Manual-only)
- **Framework**: N/A

### Manual QA Only

각 TODO에 상세한 수동 검증 절차 포함.

---

## Task Flow

```
1 (삭제: V2 migration)
    ↓
2 (삭제: CollectionTrigger entity/repository)
    ↓
3 (수정: Redis config 확인)
    ↓
4 (생성: JobQueueService)
    ↓
5 (수정: PriorityJobLauncher)
    ↓
6 (수정: CollectionTriggerListener → RedisJobQueueListener)
    ↓
7 (수정: JobSchedulingListener)
    ↓
8 (수정: UserSignupEventListener)
    ↓
9 (빌드 및 검증)
```

## Parallelization

| Group | Tasks | Reason |
|-------|-------|--------|
| A | 1, 2 | 삭제 작업, 독립적 |

| Task | Depends On | Reason |
|------|------------|--------|
| 4 | 3 | Redis 설정 확인 후 |
| 5, 6, 7, 8 | 4 | JobQueueService 필요 |
| 9 | 5, 6, 7, 8 | 모든 수정 완료 후 |

---

## TODOs

- [x] 1. V2 마이그레이션 파일 삭제

  **What to do**:
  - `backend/src/main/resources/db/migration/V2__add_collection_trigger.sql` 삭제
  - 또는 V3 마이그레이션으로 `DROP TABLE collection_trigger` 추가

  **Must NOT do**:
  - V1 마이그레이션 수정

  **Parallelizable**: YES (with 2)

  **References**:
  - `backend/src/main/resources/db/migration/V2__add_collection_trigger.sql` - 삭제 대상

  **Acceptance Criteria**:
  - [ ] `collection_trigger` 테이블이 DB에서 제거됨
  - [ ] Flyway 마이그레이션 성공: `./gradlew :backend:flywayMigrate` 또는 앱 시작 시

  **Commit**: NO (groups with 2)

---

- [x] 2. CollectionTrigger 엔티티 및 Repository 삭제

  **What to do**:
  - `common/src/main/java/io/swkoreatech/kosp/common/trigger/` 디렉토리 전체 삭제
    - `model/CollectionTrigger.java`
    - `model/TriggerPriority.java`
    - `model/TriggerStatus.java`
    - `repository/CollectionTriggerRepository.java`

  **Must NOT do**:
  - 다른 trigger 관련 코드 삭제 (harvester의 listener는 수정 대상)

  **Parallelizable**: YES (with 1)

  **References**:
  - `common/src/main/java/io/swkoreatech/kosp/common/trigger/` - 삭제 대상 디렉토리

  **Acceptance Criteria**:
  - [ ] `common/src/main/java/io/swkoreatech/kosp/common/trigger/` 디렉토리 존재하지 않음
  - [ ] `ls common/src/main/java/io/swkoreatech/kosp/common/trigger/` → "No such file or directory"

  **Commit**: YES
  - Message: `refactor(common): remove CollectionTrigger table-based queue`
  - Files: `common/src/main/java/io/swkoreatech/kosp/common/trigger/*`, `V2 or V3 migration`
  - Pre-commit: `./gradlew :common:compileJava` (실패할 수 있음, 의존 코드 수정 전)

---

- [x] 3. Redis 설정 확인

  **What to do**:
  - Harvester에 Redis 의존성 및 설정 확인
  - `StringRedisTemplate` 또는 `RedisTemplate` 사용 가능 여부 확인
  - 필요시 Redis 설정 추가

  **Must NOT do**:
  - 불필요한 Redis 설정 변경

  **Parallelizable**: NO (선행 작업)

  **References**:
  - `harvester/build.gradle.kts` - Redis 의존성 확인
  - `harvester/src/main/resources/application.yml` - Redis 설정 확인
  - `backend/src/main/java/io/swkoreatech/kosp/global/config/RedisConfig.java` - 참고용 Redis 설정

  **Acceptance Criteria**:
  - [ ] `harvester/build.gradle.kts`에 `spring-boot-starter-data-redis` 의존성 존재
  - [ ] `StringRedisTemplate` 주입 가능 확인

  **Commit**: NO (설정 변경 시에만)

---

- [x] 4. JobQueueService 생성 (common 모듈)

  **What to do**:
  - Redis Sorted Set 조작을 위한 서비스 클래스 생성
  - **common 모듈에 생성** (backend, harvester 둘 다 사용)
  - `common/src/main/java/io/swkoreatech/kosp/common/queue/JobQueueService.java`
  - `common/build.gradle.kts`에 Redis 의존성 추가
  
  ```java
  @Component
  @RequiredArgsConstructor
  public class JobQueueService {
      private static final String QUEUE_KEY = "job:queue";
      private static final long HIGH_PRIORITY_OFFSET = 0L;
      private static final long LOW_PRIORITY_OFFSET = 1_000_000_000L;
      
      private final StringRedisTemplate redisTemplate;
      
      // 작업 추가
      public void enqueue(Long userId, String runId, Instant scheduledAt, Priority priority)
      
      // 실행 가능한 작업 조회 및 제거 (ZPOPMIN with score filter)
      public Optional<JobQueueEntry> dequeue()
      
      // score 계산
      private double calculateScore(Instant scheduledAt, Priority priority)
  }
  ```

  **Must NOT do**:
  - 복잡한 추상화
  - 불필요한 메서드

  **Parallelizable**: NO (depends on 3)

  **References**:
  - `harvester/src/main/java/io/swkoreatech/kosp/launcher/Priority.java` - Priority enum
  - Spring Data Redis 문서 - ZSet 조작 방법

  **Acceptance Criteria**:
  - [ ] `common/src/main/java/io/swkoreatech/kosp/common/queue/JobQueueService.java` 존재
  - [ ] `enqueue()`, `dequeue()` 메서드 존재
  - [ ] 컴파일 성공: `./gradlew :common:compileJava`

  **Commit**: YES
  - Message: `feat(common): add Redis-based JobQueueService`
  - Files: `common/src/main/java/io/swkoreatech/kosp/common/queue/*`, `common/build.gradle.kts`
  - Pre-commit: `./gradlew :common:compileJava`

---

- [x] 5. PriorityJobLauncher 수정

  **What to do**:
  - 인메모리 `PriorityBlockingQueue` 제거
  - `processQueue()` 스케줄러 제거
  - 단순 Job 실행기로 변경
  - JobParameters: `userId` (non-identifying), `runId` (identifying)
  
  ```java
  @Component
  @RequiredArgsConstructor
  public class PriorityJobLauncher {
      private final JobLauncher jobLauncher;
      private final Job githubCollectionJob;
      private final ExecutorService executor = Executors.newSingleThreadExecutor();
      
      public void run(Long userId, String runId) {
          executor.submit(() -> executeJob(userId, runId));
      }
      
      private void executeJob(Long userId, String runId) {
          JobParameters params = new JobParametersBuilder()
              .addLong("userId", userId, false)  // non-identifying
              .addString("runId", runId, true)   // identifying
              .toJobParameters();
          jobLauncher.run(githubCollectionJob, params);
      }
  }
  ```

  **Must NOT do**:
  - `submit()` 메서드 시그니처 유지 (호출하는 곳 없어질 예정)

  **Parallelizable**: NO (depends on 4)

  **References**:
  - `harvester/src/main/java/io/swkoreatech/kosp/launcher/PriorityJobLauncher.java:81-95` - 현재 executeJob 로직
  - `harvester/src/main/java/io/swkoreatech/kosp/launcher/Priority.java` - Priority enum (삭제 가능)
  - `harvester/src/main/java/io/swkoreatech/kosp/launcher/JobLaunchRequest.java` - 삭제 대상

  **Acceptance Criteria**:
  - [ ] `PriorityBlockingQueue` 필드 없음
  - [ ] `processQueue()` 메서드 없음
  - [ ] `run(Long userId, String runId)` 메서드 존재
  - [ ] JobParameters에 `runId` (identifying) 포함

  **Commit**: YES
  - Message: `refactor(harvester): simplify PriorityJobLauncher, remove in-memory queue`
  - Files: `PriorityJobLauncher.java`, `JobLaunchRequest.java` (삭제), `Priority.java` (이동 또는 유지)
  - Pre-commit: `./gradlew :harvester:compileJava`

---

- [x] 6. CollectionTriggerListener → RedisJobQueueListener로 변경

  **What to do**:
  - 클래스명 변경: `RedisJobQueueListener`
  - Redis polling으로 변경
  - 탈퇴 사용자 체크 추가
  - `scheduleNext()` 제거 (JobSchedulingListener로 이동)
  
  ```java
  @Component
  @RequiredArgsConstructor
  public class RedisJobQueueListener {
      private final JobQueueService jobQueueService;
      private final PriorityJobLauncher jobLauncher;
      private final UserRepository userRepository;
      
      @Scheduled(fixedDelay = 1000)
      public void poll() {
          jobQueueService.dequeue().ifPresent(this::processEntry);
      }
      
      private void processEntry(JobQueueEntry entry) {
          // 탈퇴 체크
          User user = userRepository.findById(entry.userId()).orElse(null);
          if (user == null || user.isDeleted()) {
              log.info("Skipping job for deleted user: {}", entry.userId());
              return;
          }
          
          jobLauncher.run(entry.userId(), entry.runId());
      }
  }
  ```

  **Must NOT do**:
  - `CollectionTriggerRepository` 사용
  - 다음 스케줄 로직 (JobSchedulingListener에서 처리)

  **Parallelizable**: NO (depends on 4, 5)

  **References**:
  - `harvester/src/main/java/io/swkoreatech/kosp/trigger/CollectionTriggerListener.java` - 현재 구현
  - `harvester/src/main/java/io/swkoreatech/kosp/user/UserRepository.java` - 사용자 조회
  - `common/src/main/java/io/swkoreatech/kosp/common/user/model/User.java` - `isDeleted()` 메서드

  **Acceptance Criteria**:
  - [ ] `CollectionTriggerListener.java` 삭제됨
  - [ ] `RedisJobQueueListener.java` 생성됨
  - [ ] 탈퇴 체크 로직 존재
  - [ ] `jobQueueService.dequeue()` 호출

  **Commit**: YES
  - Message: `refactor(harvester): replace DB polling with Redis queue listener`
  - Files: `CollectionTriggerListener.java` (삭제), `RedisJobQueueListener.java` (생성)
  - Pre-commit: `./gradlew :harvester:compileJava`

---

- [x] 7. JobSchedulingListener 수정

  **What to do**:
  - 완료/실패 처리 로직 추가
  - Rate Limit 에러 구분
  - 다음 스케줄 Redis에 추가
  
  ```java
  @Component
  @RequiredArgsConstructor
  public class JobSchedulingListener implements JobExecutionListener {
      private final JobQueueService jobQueueService;
      private final RateLimitManager rateLimitManager;
      
      @Override
      public void afterJob(JobExecution jobExecution) {
          Long userId = jobExecution.getJobParameters().getLong("userId");
          String runId = jobExecution.getJobParameters().getString("runId");
          BatchStatus status = jobExecution.getStatus();
          
          if (status == BatchStatus.COMPLETED) {
              scheduleNextRun(userId);
              return;
          }
          
          if (isRateLimitError(jobExecution)) {
              scheduleRetry(userId, runId, getResetTimePlus5Min());
              return;
          }
          
          // 기타 에러: 30분 후 재시도
          scheduleRetry(userId, runId, Instant.now().plus(30, ChronoUnit.MINUTES));
      }
      
      private boolean isRateLimitError(JobExecution execution) {
          // 예외 체인에서 RateLimitException 확인
      }
      
      private void scheduleNextRun(Long userId) {
          String newRunId = UUID.randomUUID().toString();
          Instant nextRun = getResetTimePlus5Min();
          jobQueueService.enqueue(userId, newRunId, nextRun, Priority.LOW);
      }
      
      private void scheduleRetry(Long userId, String runId, Instant scheduledAt) {
          jobQueueService.enqueue(userId, runId, scheduledAt, Priority.HIGH);
      }
  }
  ```

  **Must NOT do**:
  - 탈퇴 체크 (RedisJobQueueListener에서 처리)
  - 복잡한 에러 분류

  **Parallelizable**: NO (depends on 4)

  **References**:
  - `harvester/src/main/java/io/swkoreatech/kosp/job/JobSchedulingListener.java` - 현재 구현
  - `harvester/src/main/java/io/swkoreatech/kosp/client/RateLimitManager.java:103-107` - `getResetTime()` 메서드
  - `harvester/src/main/java/io/swkoreatech/kosp/client/RateLimitException.java` - Rate Limit 예외 클래스

  **Acceptance Criteria**:
  - [ ] `afterJob()`에서 성공/실패 분기 처리
  - [ ] Rate Limit 에러 구분 로직 존재
  - [ ] `jobQueueService.enqueue()` 호출
  - [ ] 성공 시 새 UUID, 실패 시 동일 UUID

  **Commit**: YES
  - Message: `feat(harvester): implement job completion handling with Redis scheduling`
  - Files: `JobSchedulingListener.java`
  - Pre-commit: `./gradlew :harvester:compileJava`

---

- [x] 8. UserSignupEventListener 수정

  **What to do**:
  - `CollectionTriggerRepository` 대신 `JobQueueService` 사용
  - Redis에 즉시 실행 작업 추가
  
  ```java
  @Component
  @RequiredArgsConstructor
  public class UserSignupEventListener {
      private final JobQueueService jobQueueService;
      
      @Async
      @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
      public void handleUserSignup(UserSignupEvent event) {
          String runId = UUID.randomUUID().toString();
          jobQueueService.enqueue(
              event.getUserId(),
              runId,
              Instant.now(),  // 즉시 실행
              Priority.HIGH
          );
          log.info("Enqueued collection job for user {}", event.getUserId());
      }
  }
  ```

  **Must NOT do**:
  - `CollectionTriggerRepository` 사용

  **Parallelizable**: NO (depends on 4)

  **References**:
  - `backend/src/main/java/io/swkoreatech/kosp/domain/user/eventlistener/UserSignupEventListener.java` - 현재 구현
  - `backend/src/main/java/io/swkoreatech/kosp/domain/user/event/UserSignupEvent.java` - 이벤트 클래스

  **Acceptance Criteria**:
  - [ ] `CollectionTriggerRepository` import 없음
  - [ ] `JobQueueService.enqueue()` 호출
  - [ ] 컴파일 성공

  **Commit**: YES
  - Message: `refactor(backend): use Redis queue for signup trigger`
  - Files: `UserSignupEventListener.java`
  - Pre-commit: `./gradlew :backend:compileJava`

---

- [x] 9. 전체 빌드 및 검증

  **What to do**:
  - 전체 빌드
  - 수동 검증

  **Must NOT do**:
  - 테스트 스킵하지 않고 실패하면 수정

  **Parallelizable**: NO (마지막)

  **References**:
  - 모든 수정된 파일

  **Acceptance Criteria**:

  **빌드 검증:**
  - [ ] `./gradlew build` → BUILD SUCCESSFUL
  
  **수동 검증 (로컬 환경):**
  - [ ] Redis 실행: `docker run -d --name redis -p 6379:6379 redis`
  - [ ] Backend 시작: `./gradlew :backend:bootRun`
  - [ ] Harvester 시작: `./gradlew :harvester:bootRun`
  
  **시나리오 1: 신규 가입**
  - [ ] 회원가입 API 호출
  - [ ] Redis 확인: `redis-cli ZRANGE job:queue 0 -1 WITHSCORES`
  - [ ] Harvester 로그: "Launching job for user X" 확인
  
  **시나리오 2: Job 성공 후 다음 스케줄**
  - [ ] Job 완료 로그 확인
  - [ ] Redis 확인: 새 UUID로 다음 스케줄 추가됨
  
  **시나리오 3: 앱 재시작**
  - [ ] Harvester 중지
  - [ ] Redis 확인: 큐 데이터 유지됨
  - [ ] Harvester 재시작
  - [ ] 큐에서 작업 처리됨

  **Commit**: NO (검증만)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 2 | `refactor(common): remove CollectionTrigger table-based queue` | trigger/*, V2/V3 migration | - |
| 4 | `feat(harvester): add Redis-based JobQueueService` | queue/* | `./gradlew :harvester:compileJava` |
| 5 | `refactor(harvester): simplify PriorityJobLauncher` | launcher/* | `./gradlew :harvester:compileJava` |
| 6 | `refactor(harvester): replace DB polling with Redis queue` | trigger/*, queue/* | `./gradlew :harvester:compileJava` |
| 7 | `feat(harvester): implement job completion handling` | job/* | `./gradlew :harvester:compileJava` |
| 8 | `refactor(backend): use Redis queue for signup trigger` | eventlistener/* | `./gradlew :backend:compileJava` |

---

## Success Criteria

### Verification Commands
```bash
# 전체 빌드
./gradlew build
# Expected: BUILD SUCCESSFUL

# Redis 큐 확인
redis-cli ZRANGE job:queue 0 -1 WITHSCORES
# Expected: 등록된 작업 목록
```

### Final Checklist
- [x] `collection_trigger` 테이블 제거됨
- [x] Redis Sorted Set으로 작업 큐 관리됨
- [x] 신규 가입 시 Redis에 작업 추가됨
- [x] Job 성공/실패에 따라 적절히 다음 스케줄 설정됨
- [x] 앱 재시작 후에도 큐 유지됨
- [x] 탈퇴 사용자 스킵됨
