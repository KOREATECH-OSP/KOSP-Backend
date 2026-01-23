# GitHub 데이터 수집 시스템 전면 재설계 (V3)

## Executive Summary

현재 GitHub 데이터 수집 시스템은 **이중 실행 경로**, **인메모리 상태 관리**, **비원자적 카운터 연산** 등 근본적인 설계 결함을 갖고 있습니다. 이 문서는 **단일 실행 경로**, **DB 기반 상태 관리**, **완전 Event-driven (폴링 제로)**, **Race-free 설계**를 핵심으로 하는 전면 재설계안을 제시합니다.

### 핵심 변경 사항

| 항목 | AS-IS | TO-BE |
|------|-------|-------|
| **실행 경로** | Batch + Queue 이중화 | 단일 Orchestrator + Worker |
| **상태 저장소** | In-memory Set + Redis Counter | **MySQL (Source of Truth)** |
| **작업 분배** | Redis ZSet (상태 포함) | **Redis Streams** + MySQL (상태) |
| **완료 추적** | Counter decrement + 10초 폴링 | **Event-driven (Job 완료 시 직접 평가)** |
| **통계 트리거** | 3곳에서 중복 트리거 | **@TransactionalEventListener** |
| **Zombie 처리** | 없음 | **XAUTOCLAIM (Stream 자동 복구)** |
| **Worker 대기** | 1초 폴링 | **XREADGROUP BLOCK (무한 대기)** |
| **Rate Limit** | 분산된 처리 (Race 존재) | **Lua Atomic Reservation (Race-free)** |
| **Delayed Job 처리** | 1초 폴링 | **Sleep-until-next-due (폴링 제로)** |

### 설계 원칙: Race-free by Construction

| 문제 | 기존 방식 (Race 존재) | 새 방식 (Race-free) |
|------|----------------------|---------------------|
| Rate Limit 체크 | GET → Check → Proceed | **Lua atomic DECR** (check+mutate 원자적) |
| Job 분배 | LPOP (lost if crash) | **XREADGROUP** (pending 상태 유지) |
| Delayed → Ready 이동 | 폴링 + ZREM + RPUSH | **Lua atomic ZPOPMIN + XADD** |

---

## 1. 아키텍처 개요

### 1.1. 시스템 구성도

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                  TRIGGER LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   [UserSignupEvent]     [ScheduledTrigger]      [ManualTrigger]                     │
│          │                     │                      │                              │
│          └─────────────────────┼──────────────────────┘                              │
│                                ▼                                                     │
│                    ┌─────────────────────┐                                           │
│                    │   Orchestrator      │  ← Control Plane                          │
│                    │   (단일 진입점)      │                                           │
│                    └─────────────────────┘                                           │
│                                │                                                     │
└────────────────────────────────┼─────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              STATE STORE (MySQL)                                     │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐                  │
│   │  collection_runs │  │    page_jobs     │  │     cursors      │                  │
│   │  (사용자별 수집)  │  │   (작업 단위)     │  │  (증분 수집 위치) │                  │
│   └──────────────────┘  └──────────────────┘  └──────────────────┘                  │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 │ Job ID만 전달
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                         DISPATCH LAYER (Redis) - 폴링 제로                           │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   ┌────────────────────────────────┐    ┌────────────────────────────┐              │
│   │  github:jobs:stream (Stream)   │    │  github:jobs:delayed (ZSet) │             │
│   │  → XREADGROUP BLOCK 무한 대기   │    │  → Score = 실행 예정 시각    │             │
│   │  → XACK 성공 시 제거            │    │  → Sleep-until-next-due     │             │
│   │  → XAUTOCLAIM 자동 복구         │    │                              │             │
│   └────────────────────────────────┘    └────────────────────────────┘              │
│                 ▲                                    │                               │
│                 │                                    │ Lua atomic promote           │
│                 │                                    │ (ZPOPMIN + XADD)             │
│                 └────────────────────────────────────┘                               │
│                                                                                      │
│   ┌────────────────────────────────────────────────────────────────────────────┐    │
│   │  Rate Limit: github:ratelimit:{token_hash}                                  │    │
│   │  → Lua Atomic Reservation: DECR + check + conditional rollback              │    │
│   │  → Race condition 원천 불가능                                                │    │
│   └────────────────────────────────────────────────────────────────────────────┘    │
│                                                                                      │
│   ┌────────────────────────────────────────────────────────────────────────────┐    │
│   │  Token Job Queue: github:token:{hash}:jobs (List)                           │    │
│   │  → 토큰별 Job 관리 (Rate Limit 시 토큰 단위로 지연)                           │    │
│   └────────────────────────────────────────────────────────────────────────────┘    │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 │ XREADGROUP BLOCK (무한 대기)
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              WORKER LAYER                                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                                 │
│   │  Worker 1   │  │  Worker 2   │  │  Worker N   │  ← Data Plane                   │
│   └─────────────┘  └─────────────┘  └─────────────┘                                 │
│          │                │                │                                         │
│          └────────────────┼────────────────┘                                         │
│                           │                                                          │
│                           ▼                                                          │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │                    RateLimitReservation (Lua Atomic)                         │   │
│   │  - reserve(tokenHash, permits=1) → (allowed, waitMs)                        │   │
│   │  - Race condition 불가능 (single Lua execution)                              │   │
│   │  - 실패 시 자동으로 대기 시간 반환                                            │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│                           │                                                          │
│                           ▼                                                          │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │                         GitHub API Client                                    │   │
│   │  - Pagination 처리                                                           │   │
│   │  - Response 헤더로 Rate Limit 업데이트                                        │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│                           │                                                          │
│                           ▼                                                          │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │                         MongoDB Writer                                       │   │
│   │  - Idempotent Upsert (deterministic ID 사용)                                 │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│                           │                                                          │
│                           │ XACK (성공) 또는 재스케줄 (실패)                         │
│                           ▼                                                          │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │                    RunCompletionEvaluator (동기 호출)                         │   │
│   │  - 해당 Run의 pending job 수 확인                                            │   │
│   │  - 0이면 Run 완료 처리 + 통계 이벤트 발행                                     │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 │ @TransactionalEventListener
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           STATISTICS LAYER                                           │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   RunCompletedEvent  ──(@TransactionalEventListener)──▶  StatisticsService          │
│                                                                                      │
│   - 트랜잭션 커밋 후 자동 실행                                                       │
│   - 단일 트리거 지점 (중복 없음)                                                     │
│   - 실패 시 별도 재시도 큐로 이동                                                    │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                      SCHEDULER (폴링 제로 - Sleep-until-next-due)                    │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   [DelayedJobScheduler] - Leader Election 기반 단일 실행                             │
│                                                                                      │
│   while (true) {                                                                     │
│       nextDueTime = ZRANGE delayed 0 0 WITHSCORES  // 가장 빠른 실행 시각            │
│       if (nextDueTime == null) {                                                     │
│           wait(NOTIFY)  // Pub/Sub으로 새 Job 알림 대기                              │
│       } else {                                                                       │
│           sleepUntil(nextDueTime)  // 정확한 시각까지 sleep                          │
│           Lua: ZPOPMIN + XADD (atomic promote)                                       │
│       }                                                                              │
│   }                                                                                  │
│                                                                                      │
│   ※ 고정 간격 폴링 없음 - CPU 0%                                                    │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 1.2. 핵심 설계 원칙

| 원칙 | 설명 |
|------|------|
| **Single Source of Truth** | MySQL이 모든 상태의 원본. Redis는 dispatch 용도로만 사용 |
| **Atomic Operations Only** | 모든 Redis 연산은 Lua 스크립트로 원자적 실행 |
| **Zero Polling** | 모든 대기는 blocking (XREADGROUP, sleep-until) |
| **Race-free by Construction** | 설계 자체가 race condition을 불가능하게 함 |
| **Idempotent Operations** | 모든 쓰기 작업은 재실행해도 동일 결과 |
| **Page-level Granularity** | API 1회 호출 = 1 Job → 세밀한 재시도/모니터링 |

### 1.3. 폴링 완전 제거

| 기존 컴포넌트 | 기존 방식 | 변경 후 |
|--------------|----------|---------|
| **Worker** | 1초 폴링 | `XREADGROUP BLOCK 0` (무한 대기) |
| **Reaper** | 30초 폴링 | **제거** → `XAUTOCLAIM` 자동 복구 |
| **RunEvaluator** | 10초 폴링 | Job 완료 시 동기 호출 |
| **OutboxPublisher** | 1초 폴링 | **제거** → `@TransactionalEventListener` |
| **DelayedQueuePromoter** | 1초 폴링 | **Sleep-until-next-due** (폴링 제로) |

---

## 2. 데이터 모델 (MySQL)

### 2.1. collection_runs (사용자별 수집 실행)

```sql
CREATE TABLE collection_runs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    github_login    VARCHAR(100) NOT NULL,
    
    -- 상태
    status          VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    -- CREATED → PLANNING → RUNNING → FINALIZING → COMPLETED / FAILED / CANCELED
    
    -- 메타데이터
    triggered_by    VARCHAR(50) NOT NULL,  -- 'SIGNUP', 'SCHEDULED', 'MANUAL'
    
    -- 타임스탬프
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at      TIMESTAMP NULL,
    completed_at    TIMESTAMP NULL,
    
    -- 통계
    total_jobs      INT NOT NULL DEFAULT 0,
    succeeded_jobs  INT NOT NULL DEFAULT 0,
    failed_jobs     INT NOT NULL DEFAULT 0,
    
    -- 에러 정보
    last_error      TEXT NULL,
    
    -- 인덱스
    INDEX idx_user_status (user_id, status),
    INDEX idx_status_created (status, created_at),
    
    -- 동일 사용자 중복 실행 방지 (선택적)
    UNIQUE INDEX uk_user_active (user_id, status) 
        WHERE status IN ('CREATED', 'PLANNING', 'RUNNING')
);
```

### 2.2. page_jobs (작업 단위) - V3 간소화

```sql
CREATE TABLE page_jobs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_id          BIGINT NOT NULL,
    
    -- 작업 식별
    job_type        VARCHAR(30) NOT NULL,
    -- USER_BASIC, USER_EVENTS, REPO_ISSUES, REPO_PRS, REPO_COMMITS
    
    resource_key    VARCHAR(255) NOT NULL,  -- 'user:octocat' or 'repo:owner/name'
    page_key        VARCHAR(100) NULL,      -- 페이지/커서 식별자 (첫 페이지는 NULL)
    
    -- 상태 (V3: 간소화)
    status          VARCHAR(20) NOT NULL DEFAULT 'READY',
    -- READY → ENQUEUED → RUNNING → SUCCEEDED / FAILED
    -- ※ RETRYABLE_FAILED 제거: 재시도는 READY + delayed queue로 처리
    
    -- ※ V3 제거된 컬럼 (Redis Streams가 처리):
    -- worker_id       VARCHAR(100) NULL,   -- Streams Consumer ID로 대체
    -- lease_until     TIMESTAMP NULL,      -- XAUTOCLAIM으로 대체
    
    -- 재시도
    attempt_count   INT NOT NULL DEFAULT 0,
    max_attempts    INT NOT NULL DEFAULT 5,
    not_before      TIMESTAMP NULL,  -- Rate limit으로 인한 대기
    
    -- 페이지네이션
    next_cursor     VARCHAR(500) NULL,  -- 다음 페이지 커서 (완료 후 설정)
    
    -- 타임스탬프
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at      TIMESTAMP NULL,
    completed_at    TIMESTAMP NULL,
    
    -- 에러 정보
    last_error      TEXT NULL,
    
    -- 외래키
    CONSTRAINT fk_page_jobs_run FOREIGN KEY (run_id) REFERENCES collection_runs(id),
    
    -- 인덱스 (V3: lease_expired 인덱스 제거)
    INDEX idx_run_status (run_id, status),
    INDEX idx_status_not_before (status, not_before),
    
    -- 중복 방지
    UNIQUE INDEX uk_run_type_resource_page (run_id, job_type, resource_key, page_key)
);
```

> **V3 변경사항**:
> - `worker_id`, `lease_until` 컬럼 제거 → Redis Streams Consumer Group이 추적
> - `RETRYABLE_FAILED` 상태 제거 → `READY` + `not_before` + delayed queue로 대체
> - `idx_lease_expired` 인덱스 제거 → XAUTOCLAIM이 복구 담당

### 2.3. collection_cursors (증분 수집 위치)

```sql
CREATE TABLE collection_cursors (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    
    -- 리소스 식별
    resource_type   VARCHAR(30) NOT NULL,  -- USER_EVENTS, REPO_ISSUES, REPO_PRS, REPO_COMMITS
    resource_key    VARCHAR(255) NOT NULL, -- 'user:octocat' or 'repo:owner/name'
    
    -- 커서 정보
    last_cursor     VARCHAR(500) NULL,     -- 마지막 페이지 커서
    last_timestamp  TIMESTAMP NULL,        -- 마지막 수집 시점 (since 파라미터용)
    last_etag       VARCHAR(100) NULL,     -- ETag (304 Not Modified 활용)
    
    -- 메타데이터
    last_run_id     BIGINT NULL,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 유니크 제약
    UNIQUE INDEX uk_user_resource (user_id, resource_type, resource_key)
);
```

### 2.4. ~~outbox~~ (제거됨)

> **Note**: Outbox 패턴 대신 `@TransactionalEventListener`를 사용합니다.
> 
> - 트랜잭션 커밋 후 자동으로 이벤트 핸들러 실행
> - 별도 테이블/폴링 불필요
> - 실패 시 `StatisticsRetryQueue` (Redis ZSet)로 재시도

---

## 3. Redis 키 설계 (V3)

```
github:jobs:stream              # Stream - XREADGROUP BLOCK 무한 대기
github:jobs:delayed             # ZSet - Score = executeAt (epoch ms)
github:ratelimit:{tokenHash}    # Hash - {remaining, resetAt}
github:scheduler:notify         # Pub/Sub channel - Scheduler wake-up
github:scheduler:lock           # String - Leader election (TTL 30초)
github:stats:retry              # ZSet - 통계 재시도 큐
```

| 키 | 타입 | 용도 | TTL |
|----|------|------|-----|
| `github:jobs:stream` | Stream | 즉시 실행 Job 큐 | MINID trim |
| `github:jobs:delayed` | ZSet | 지연 실행 Job (Rate Limit, Retry) | - |
| `github:ratelimit:{hash}` | Hash | 토큰별 Rate Limit 상태 | 2시간 |
| `github:scheduler:notify` | Pub/Sub | Delayed 추가 시 Scheduler 알림 | - |
| `github:scheduler:lock` | String | Leader election lock | 30초 |
| `github:stats:retry` | ZSet | 통계 계산 재시도 | - |

---

## 4. Lua 스크립트 (Atomic Operations)

모든 Redis 연산은 Lua 스크립트로 원자적 실행됩니다. Race condition이 **설계상 불가능**합니다.

### 4.1. Rate Limit Reservation (핵심)

```lua
-- KEYS[1] = github:ratelimit:{tokenHash}
-- ARGV[1] = permits (요청할 횟수, 보통 1)
-- ARGV[2] = threshold (이 이하면 대기, 예: 100)
-- ARGV[3] = current_time_ms
--
-- Returns: {allowed (0/1), waitMs, remaining}
-- - allowed=1: 진행 가능, remaining 반환
-- - allowed=0: 대기 필요, waitMs 반환 (resetAt - now)

local key = KEYS[1]
local permits = tonumber(ARGV[1])
local threshold = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- 현재 상태 조회
local remaining = tonumber(redis.call('HGET', key, 'remaining'))
local resetAt = tonumber(redis.call('HGET', key, 'resetAt'))

-- 정보 없음 → 허용 (첫 요청)
if remaining == nil then
    return {1, 0, -1}  -- allowed, waitMs=0, remaining=unknown
end

-- Reset 시각이 지났으면 quota 리셋
if now >= resetAt then
    redis.call('DEL', key)
    return {1, 0, -1}  -- 리셋됨, 허용
end

-- 남은 quota 체크 (threshold 고려)
if remaining > threshold + permits then
    -- ★ Atomic decrement: check와 mutate가 한 번에
    local newRemaining = redis.call('HINCRBY', key, 'remaining', -permits)
    return {1, 0, newRemaining}  -- 허용
else
    -- Rate limit 임박/도달 → 대기 필요
    local waitMs = resetAt - now
    return {0, waitMs, remaining}  -- 거부, 대기 시간 반환
end
```

### 4.2. Rate Limit Update (API 응답 후)

```lua
-- KEYS[1] = github:ratelimit:{tokenHash}
-- ARGV[1] = remaining (from X-RateLimit-Remaining header)
-- ARGV[2] = resetAt (from X-RateLimit-Reset header, in ms)
-- ARGV[3] = ttl_seconds
--
-- GitHub 응답이 현재 상태보다 "신선"한 경우에만 업데이트
-- Out-of-order 응답으로 인한 상태 corruption 방지

local key = KEYS[1]
local newRemaining = tonumber(ARGV[1])
local newResetAt = tonumber(ARGV[2])
local ttl = tonumber(ARGV[3])

local currentResetAt = tonumber(redis.call('HGET', key, 'resetAt'))

-- 새 resetAt이 더 최신이면 덮어쓰기
if currentResetAt == nil or newResetAt >= currentResetAt then
    redis.call('HSET', key, 'remaining', newRemaining, 'resetAt', newResetAt)
    redis.call('EXPIRE', key, ttl)
    return 1
else
    -- 기존 값 유지 (out-of-order 응답 무시)
    -- 단, remaining이 더 작으면 보수적으로 업데이트
    local currentRemaining = tonumber(redis.call('HGET', key, 'remaining'))
    if currentRemaining == nil or newRemaining < currentRemaining then
        redis.call('HSET', key, 'remaining', newRemaining)
    end
    return 0
end
```

### 4.3. Delayed → Ready Promotion (Atomic)

```lua
-- KEYS[1] = github:jobs:delayed (ZSet)
-- KEYS[2] = github:jobs:stream (Stream)
-- ARGV[1] = current_time_ms
-- ARGV[2] = max_count (한 번에 이동할 최대 Job 수)
--
-- Returns: 이동된 Job 수

local delayedKey = KEYS[1]
local streamKey = KEYS[2]
local now = tonumber(ARGV[1])
local maxCount = tonumber(ARGV[2])

-- 실행 시각이 도래한 Job들 조회 (score <= now)
local jobs = redis.call('ZRANGEBYSCORE', delayedKey, '-inf', now, 'LIMIT', 0, maxCount)

if #jobs == 0 then
    return 0
end

-- Atomic: ZREM + XADD
for i, jobData in ipairs(jobs) do
    -- ZSet에서 제거
    redis.call('ZREM', delayedKey, jobData)
    -- Stream에 추가
    redis.call('XADD', streamKey, '*', 'jobId', jobData)
end

return #jobs
```

### 4.4. Scheduler Wake-up Notification

```lua
-- KEYS[1] = github:jobs:delayed (ZSet)
-- KEYS[2] = github:scheduler:notify (Pub/Sub channel)
-- ARGV[1] = jobId
-- ARGV[2] = executeAt (ms)
--
-- Job을 delayed queue에 추가하고, 
-- 만약 이 Job이 가장 빠른 실행 시각이면 Scheduler에 알림

local delayedKey = KEYS[1]
local notifyChannel = KEYS[2]
local jobId = ARGV[1]
local executeAt = tonumber(ARGV[2])

-- Job 추가
redis.call('ZADD', delayedKey, executeAt, jobId)

-- 가장 빠른 실행 시각 조회
local earliest = redis.call('ZRANGE', delayedKey, 0, 0, 'WITHSCORES')

-- 방금 추가한 Job이 가장 빠르면 Scheduler 깨우기
if earliest[1] == jobId then
    redis.call('PUBLISH', notifyChannel, executeAt)
end

return 1
```

---

## 5. 컴포넌트 상세 설계

### 5.0. Rate Limit Reservation (Race-free)

```java
/**
 * Race-free Rate Limit 관리.
 * Lua 스크립트로 check + mutate를 원자적으로 실행.
 * 
 * ★ 핵심: canProceed() 대신 reserve() 사용
 * - 기존: canProceed() → true → API 호출 (Race 존재)
 * - 신규: reserve() → (allowed, waitMs) 원자적 반환
 */
@Component
@RequiredArgsConstructor
public class RateLimitReservation {
    
    private static final int DEFAULT_THRESHOLD = 100;
    private static final int DEFAULT_TTL_SECONDS = 7200;  // 2시간
    
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List<Long>> reserveScript;
    private final RedisScript<Long> updateScript;
    
    /**
     * Rate Limit quota 예약 (원자적)
     * 
     * @param tokenHash 토큰 해시
     * @param permits 요청할 횟수 (기본 1)
     * @return ReservationResult (allowed, waitMs, remaining)
     */
    public ReservationResult reserve(String tokenHash, int permits) {
        String key = "github:ratelimit:" + tokenHash;
        long now = System.currentTimeMillis();
        
        List<Long> result = redisTemplate.execute(
            reserveScript,
            List.of(key),
            String.valueOf(permits),
            String.valueOf(DEFAULT_THRESHOLD),
            String.valueOf(now)
        );
        
        boolean allowed = result.get(0) == 1;
        long waitMs = result.get(1);
        long remaining = result.get(2);
        
        return new ReservationResult(allowed, waitMs, remaining);
    }
    
    /**
     * Rate Limit quota 예약 (기본 1 permit)
     */
    public ReservationResult reserve(String tokenHash) {
        return reserve(tokenHash, 1);
    }
    
    /**
     * API 응답 헤더로 Rate Limit 상태 업데이트
     */
    public void updateFromHeaders(String tokenHash, HttpHeaders headers) {
        String remaining = headers.getFirst("X-RateLimit-Remaining");
        String reset = headers.getFirst("X-RateLimit-Reset");
        
        if (remaining == null || reset == null) {
            return;
        }
        
        String key = "github:ratelimit:" + tokenHash;
        long resetAtMs = Long.parseLong(reset) * 1000;
        
        redisTemplate.execute(
            updateScript,
            List.of(key),
            remaining,
            String.valueOf(resetAtMs),
            String.valueOf(DEFAULT_TTL_SECONDS)
        );
    }
    
    public record ReservationResult(
        boolean allowed,
        long waitMs,
        long remaining
    ) {
        public boolean shouldWait() {
            return !allowed && waitMs > 0;
        }
    }
}
```

### 5.1. Job Dispatcher (Redis Streams)

```java
/**
 * Redis Streams 기반 Job 분배.
 * 
 * List + BLPOP 대비 장점:
 * - XACK 전까지 메시지 보존 (crash-safe)
 * - XAUTOCLAIM으로 stuck job 자동 복구
 * - Consumer Group으로 부하 분산
 */
@Component
@RequiredArgsConstructor
public class JobDispatcher {
    
    private static final String STREAM_KEY = "github:jobs:stream";
    private static final String GROUP_NAME = "workers";
    private static final String DELAYED_KEY = "github:jobs:delayed";
    private static final String NOTIFY_CHANNEL = "github:scheduler:notify";
    
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> promoteScript;
    private final RedisScript<Long> addDelayedScript;
    
    @PostConstruct
    public void initConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
        } catch (RedisSystemException e) {
            // Group already exists - OK
        }
    }
    
    /**
     * Job을 즉시 실행 Stream에 추가
     */
    public void dispatch(Long jobId) {
        redisTemplate.opsForStream().add(
            STREAM_KEY,
            Map.of("jobId", jobId.toString())
        );
    }
    
    /**
     * Job을 지연 실행 큐에 추가 + Scheduler 알림
     */
    public void dispatchDelayed(Long jobId, long executeAtMs) {
        redisTemplate.execute(
            addDelayedScript,
            List.of(DELAYED_KEY, NOTIFY_CHANNEL),
            jobId.toString(),
            String.valueOf(executeAtMs)
        );
    }
    
    /**
     * Stream에서 Job 가져오기 (무한 대기)
     * 
     * @param consumerId Worker 고유 ID
     * @param timeout 대기 시간 (0 = 무한)
     * @return Job ID or null (timeout)
     */
    public JobMessage blockingRead(String consumerId, Duration timeout) {
        List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
            .read(
                Consumer.from(GROUP_NAME, consumerId),
                StreamReadOptions.empty()
                    .count(1)
                    .block(timeout),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
            );
        
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        
        MapRecord<String, Object, Object> record = messages.get(0);
        String jobId = (String) record.getValue().get("jobId");
        
        return new JobMessage(
            record.getId().getValue(),  // Stream message ID (for XACK)
            Long.parseLong(jobId)
        );
    }
    
    /**
     * Job 처리 완료 확인 (Stream에서 제거)
     */
    public void acknowledge(String messageId) {
        redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, messageId);
    }
    
    /**
     * Stuck Job 자동 복구 (XAUTOCLAIM)
     * Worker 시작 시 또는 주기적으로 호출
     */
    public List<JobMessage> claimStuckJobs(String consumerId, Duration minIdleTime) {
        // XAUTOCLAIM: minIdleTime 이상 pending 상태인 메시지를 현재 consumer로 이전
        List<MapRecord<String, Object, Object>> claimed = redisTemplate.opsForStream()
            .claim(
                STREAM_KEY,
                Consumer.from(GROUP_NAME, consumerId),
                minIdleTime,
                MessageId.of("0-0")  // Start from beginning
            );
        
        return claimed.stream()
            .map(r -> new JobMessage(
                r.getId().getValue(),
                Long.parseLong((String) r.getValue().get("jobId"))
            ))
            .toList();
    }
    
    public record JobMessage(String messageId, Long jobId) {}
}
```

### 5.2. Delayed Job Scheduler (폴링 제로)

```java
/**
 * Delayed Queue 스케줄러.
 * 
 * ★ 폴링 제로 설계:
 * - 고정 간격 폴링 없음
 * - 다음 실행 시각까지 정확히 sleep
 * - 새 Job 추가 시 Pub/Sub으로 즉시 알림
 * 
 * Leader Election으로 단일 인스턴스만 실행
 */
@Component
@RequiredArgsConstructor
public class DelayedJobScheduler {
    
    private static final String DELAYED_KEY = "github:jobs:delayed";
    private static final String NOTIFY_CHANNEL = "github:scheduler:notify";
    private static final String LOCK_KEY = "github:scheduler:lock";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> promoteScript;
    private final JobDispatcher jobDispatcher;
    
    private volatile boolean running = true;
    private final Object wakeupSignal = new Object();
    
    @PostConstruct
    public void start() {
        // Pub/Sub 리스너 등록
        redisTemplate.getConnectionFactory().getConnection()
            .subscribe((message, pattern) -> {
                synchronized (wakeupSignal) {
                    wakeupSignal.notify();  // sleep 중인 스케줄러 깨우기
                }
            }, NOTIFY_CHANNEL.getBytes());
        
        // 스케줄러 스레드 시작
        Thread schedulerThread = new Thread(this::schedulerLoop, "delayed-job-scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }
    
    private void schedulerLoop() {
        while (running) {
            // Leader lock 획득 시도
            Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, "1", LOCK_TTL);
            
            if (Boolean.FALSE.equals(acquired)) {
                // 다른 인스턴스가 Leader - 잠시 대기 후 재시도
                sleep(Duration.ofSeconds(5));
                continue;
            }
            
            try {
                runAsLeader();
            } finally {
                redisTemplate.delete(LOCK_KEY);
            }
        }
    }
    
    private void runAsLeader() {
        while (running) {
            // Lock 연장
            redisTemplate.expire(LOCK_KEY, LOCK_TTL);
            
            // 가장 빠른 실행 시각 조회
            Set<ZSetOperations.TypedTuple<String>> earliest = redisTemplate.opsForZSet()
                .rangeWithScores(DELAYED_KEY, 0, 0);
            
            if (earliest == null || earliest.isEmpty()) {
                // 대기 중인 Job 없음 → Pub/Sub 알림 대기
                waitForNotification(Duration.ofSeconds(30));
                continue;
            }
            
            ZSetOperations.TypedTuple<String> next = earliest.iterator().next();
            long executeAt = next.getScore().longValue();
            long now = System.currentTimeMillis();
            
            if (executeAt > now) {
                // 아직 시간 안됨 → 정확한 시각까지 sleep
                long sleepMs = executeAt - now;
                waitForNotification(Duration.ofMillis(Math.min(sleepMs, 30000)));
                continue;
            }
            
            // 실행 시각 도래 → Lua로 atomic promote
            long promoted = promoteReadyJobs();
            
            if (promoted > 0) {
                log.debug("Promoted {} jobs from delayed to ready", promoted);
            }
        }
    }
    
    private long promoteReadyJobs() {
        Long result = redisTemplate.execute(
            promoteScript,
            List.of(DELAYED_KEY, "github:jobs:stream"),
            String.valueOf(System.currentTimeMillis()),
            "100"  // max count
        );
        return result != null ? result : 0;
    }
    
    private void waitForNotification(Duration timeout) {
        synchronized (wakeupSignal) {
            try {
                wakeupSignal.wait(timeout.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @PreDestroy
    public void stop() {
        running = false;
        synchronized (wakeupSignal) {
            wakeupSignal.notifyAll();
        }
    }
}
```

### 5.3. Collection Worker (Redis Streams)

```java
/**
 * Redis Streams 기반 Worker.
 * 
 * 기존 BLPOP 대비 장점:
 * - Crash 시에도 메시지 유실 없음 (pending 상태 유지)
 * - XAUTOCLAIM으로 stuck job 자동 복구
 * - 명시적 XACK로 성공만 제거
 */
@Component
@RequiredArgsConstructor
public class CollectionWorker {
    
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration STUCK_THRESHOLD = Duration.ofMinutes(5);
    
    private final JobDispatcher jobDispatcher;
    private final PageJobRepository jobRepository;
    private final RateLimitReservation rateLimitReservation;
    private final GithubApiClient githubClient;
    private final MongoDataWriter mongoWriter;
    private final RunCompletionEvaluator completionEvaluator;
    
    private final String consumerId = UUID.randomUUID().toString();
    
    /**
     * Worker 메인 루프
     */
    @Async("workerExecutor")
    public void startWorkerLoop() {
        // 시작 시 stuck job 복구
        recoverStuckJobs();
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processNextJob();
            } catch (Exception e) {
                log.error("Worker loop error", e);
            }
        }
    }
    
    private void recoverStuckJobs() {
        List<JobDispatcher.JobMessage> stuckJobs = 
            jobDispatcher.claimStuckJobs(consumerId, STUCK_THRESHOLD);
        
        for (JobDispatcher.JobMessage msg : stuckJobs) {
            log.warn("Recovered stuck job: {}", msg.jobId());
            processJob(msg);
        }
    }
    
    private void processNextJob() {
        // XREADGROUP BLOCK: Job이 올 때까지 무한 대기
        JobDispatcher.JobMessage message = jobDispatcher.blockingRead(consumerId, READ_TIMEOUT);
        
        if (message == null) {
            return;  // Timeout - 다시 대기
        }
        
        processJob(message);
    }
    
    private void processJob(JobDispatcher.JobMessage message) {
        Long jobId = message.jobId();
        String messageId = message.messageId();
        
        PageJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            // Job이 없으면 ACK하고 스킵
            jobDispatcher.acknowledge(messageId);
            return;
        }
        
        String tokenHash = hashToken(job.getEncryptedToken());
        
        try {
            // 1. Rate Limit Reservation (Atomic)
            RateLimitReservation.ReservationResult reservation = 
                rateLimitReservation.reserve(tokenHash);
            
            if (!reservation.allowed()) {
                // Rate Limit → Delayed Queue로 이동
                rescheduleForRateLimit(job, messageId, reservation.waitMs());
                return;
            }
            
            // 2. DB 상태 업데이트 (RUNNING)
            job.markRunning(consumerId);
            jobRepository.save(job);
            
            // 3. GitHub API 호출
            ApiResponse response = githubClient.fetch(job.toApiRequest());
            
            // 4. Rate Limit 업데이트
            rateLimitReservation.updateFromHeaders(tokenHash, response.getHeaders());
            
            // 5. MongoDB 저장
            mongoWriter.write(job.getRunId(), job.getJobType(), response.getData());
            
            // 6. 성공 처리
            handleSuccess(job, messageId, response);
            
        } catch (RateLimitException e) {
            handleRateLimit(job, messageId, tokenHash, e);
        } catch (RecoverableException e) {
            handleRetryableFailure(job, messageId, e);
        } catch (UnrecoverableException e) {
            handlePermanentFailure(job, messageId, e);
        }
    }
    
    private void handleSuccess(PageJob job, String messageId, ApiResponse response) {
        job.markSucceeded();
        job.setNextCursor(response.getNextCursor());
        jobRepository.save(job);
        
        // Stream에서 제거 (성공)
        jobDispatcher.acknowledge(messageId);
        
        // 다음 페이지 Job 생성
        if (response.hasNextPage()) {
            createNextPageJob(job, response.getNextCursor());
        }
        
        // Run 완료 평가
        completionEvaluator.evaluateRunCompletion(job.getRunId());
    }
    
    private void rescheduleForRateLimit(PageJob job, String messageId, long waitMs) {
        long executeAt = System.currentTimeMillis() + waitMs;
        
        job.setNotBefore(Instant.ofEpochMilli(executeAt));
        job.markReady();
        jobRepository.save(job);
        
        // Stream에서 제거 (재스케줄)
        jobDispatcher.acknowledge(messageId);
        
        // Delayed Queue에 추가
        jobDispatcher.dispatchDelayed(job.getId(), executeAt);
        
        log.info("Job {} rescheduled for rate limit, wait {}ms", job.getId(), waitMs);
    }
    
    private void handleRateLimit(PageJob job, String messageId, String tokenHash, RateLimitException e) {
        long waitMs = e.getResetTime().toEpochMilli() - System.currentTimeMillis();
        rescheduleForRateLimit(job, messageId, Math.max(waitMs, 1000));
    }
    
    private void handleRetryableFailure(PageJob job, String messageId, RecoverableException e) {
        job.setLastError(e.getMessage());
        job.incrementAttempt();
        
        if (job.canRetry()) {
            // Exponential backoff
            long backoffMs = (long) Math.pow(2, job.getAttemptCount()) * 1000;
            long executeAt = System.currentTimeMillis() + backoffMs;
            
            job.setNotBefore(Instant.ofEpochMilli(executeAt));
            job.markReady();
            jobRepository.save(job);
            
            jobDispatcher.acknowledge(messageId);
            jobDispatcher.dispatchDelayed(job.getId(), executeAt);
            
            log.warn("Job {} failed, retry {} after {}ms", job.getId(), job.getAttemptCount(), backoffMs);
        } else {
            handlePermanentFailure(job, messageId, e);
        }
    }
    
    private void handlePermanentFailure(PageJob job, String messageId, Exception e) {
        job.setLastError(e.getMessage());
        job.markFailed();
        jobRepository.save(job);
        
        jobDispatcher.acknowledge(messageId);
        
        // 실패한 Job도 Run 완료 평가 대상
        completionEvaluator.evaluateRunCompletion(job.getRunId());
        
        log.error("Job {} permanently failed: {}", job.getId(), e.getMessage());
    }
}
```

### 5.4. Orchestrator (제어 평면)

```java
/**
 * 수집 작업의 전체 흐름을 조율하는 컴포넌트.
 * 직접 GitHub API를 호출하지 않음.
 */
@Service
@RequiredArgsConstructor
public class CollectionOrchestrator {
    
    private final CollectionRunRepository runRepository;
    private final PageJobRepository jobRepository;
    private final CollectionCursorRepository cursorRepository;
    private final JobDispatcher jobDispatcher;
    
    /**
     * 새로운 수집 실행 시작
     */
    @Transactional
    public CollectionRun startCollection(Long userId, String githubLogin, TriggerType trigger) {
        // 1. 중복 실행 체크
        if (runRepository.existsActiveRun(userId)) {
            throw new DuplicateCollectionException(userId);
        }
        
        // 2. Run 생성
        CollectionRun run = CollectionRun.create(userId, githubLogin, trigger);
        runRepository.save(run);
        
        // 3. Planning 시작
        planJobs(run);
        
        return run;
    }
    
    /**
     * Job 목록 생성 (Planning 단계)
     */
    private void planJobs(CollectionRun run) {
        run.startPlanning();
        
        // User-level jobs
        createJob(run, JobType.USER_BASIC, "user:" + run.getGithubLogin(), null);
        createJob(run, JobType.USER_EVENTS, "user:" + run.getGithubLogin(), null);
        
        // Repository jobs는 USER_BASIC 완료 후 동적 생성
        // → PageJobCompletionHandler에서 처리
        
        run.startRunning();
        runRepository.save(run);
        
        // Ready 상태 Job들을 Redis Stream으로 dispatch
        dispatchReadyJobs(run.getId());
    }
    
    /**
     * Job 생성 (중복 방지)
     */
    private void createJob(CollectionRun run, JobType type, String resourceKey, String pageKey) {
        // 중복 체크 (UK 제약으로도 보장되지만 명시적 체크)
        if (jobRepository.existsByRunAndTypeAndResource(run.getId(), type, resourceKey, pageKey)) {
            return;
        }
        
        PageJob job = PageJob.create(run, type, resourceKey, pageKey);
        
        // 증분 수집: 이전 커서 조회
        cursorRepository.findByUserAndResource(run.getUserId(), type, resourceKey)
            .ifPresent(cursor -> job.setSince(cursor.getLastTimestamp()));
        
        jobRepository.save(job);
        run.incrementTotalJobs();
    }
    
    /**
     * Ready 상태 Job들을 Redis Stream으로 dispatch
     */
    @Transactional
    public void dispatchReadyJobs(Long runId) {
        List<PageJob> readyJobs = jobRepository.findReadyJobs(runId);
        
        for (PageJob job : readyJobs) {
            job.markEnqueued();
            jobRepository.save(job);
            jobDispatcher.dispatch(job.getId());
        }
    }
}
```

### 5.5. RunCompletionEvaluator (폴링 없는 완료 평가)

```java
/**
 * Run의 완료 여부를 평가하고 상태를 전이시키는 서비스.
 * Worker가 Job 완료 시 직접 호출 → 폴링 제거.
 */
@Component
@RequiredArgsConstructor
public class RunCompletionEvaluator {
    
    private final CollectionRunRepository runRepository;
    private final PageJobRepository jobRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Run 완료 여부 평가 (Worker에서 동기 호출)
     */
    @Transactional
    public void evaluateRunCompletion(Long runId) {
        CollectionRun run = runRepository.findById(runId).orElse(null);
        
        if (run == null || run.getStatus() != RunStatus.RUNNING) {
            return;
        }
        
        // 남은 Job 수 확인 (DB 쿼리)
        long pendingJobs = jobRepository.countPendingJobs(runId);
        
        if (pendingJobs > 0) {
            return;  // 아직 진행 중
        }
        
        // 모든 Job 완료 → COMPLETED
        long succeededJobs = jobRepository.countByRunIdAndStatus(runId, JobStatus.SUCCEEDED);
        long failedJobs = jobRepository.countByRunIdAndStatus(runId, JobStatus.FAILED);
        
        run.complete(succeededJobs, failedJobs);
        runRepository.save(run);
        
        log.info("Run {} completed. Succeeded: {}, Failed: {}", runId, succeededJobs, failedJobs);
        
        // ★ 이벤트 발행 (트랜잭션 커밋 후 처리됨)
        eventPublisher.publishEvent(new RunCompletedEvent(run));
    }
}

/**
 * Run 완료 이벤트
 */
public record RunCompletedEvent(CollectionRun run) {}
```

### 5.6. 통계 계산 (Event-driven)

```java
/**
 * Run 완료 시 통계 계산을 트리거하는 리스너.
 * @TransactionalEventListener로 트랜잭션 커밋 후 실행.
 */
@Component
@RequiredArgsConstructor
public class StatisticsEventListener {
    
    private final StatisticsService statisticsService;
    private final StatisticsRetryQueue retryQueue;
    
    /**
     * Run 완료 시 통계 계산 (트랜잭션 커밋 후 실행)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRunCompleted(RunCompletedEvent event) {
        CollectionRun run = event.run();
        
        try {
            statisticsService.calculateUserStatistics(run.getUserId(), run.getGithubLogin());
            statisticsService.calculateGlobalStatistics();
            
            log.info("Statistics calculated for user: {}", run.getGithubLogin());
            
        } catch (Exception e) {
            // 실패 시 재시도 큐에 등록
            log.error("Statistics calculation failed for run {}: {}", run.getId(), e.getMessage());
            retryQueue.enqueue(run.getId());
        }
    }
}
```

---

## 6. 상태 머신

### 6.1. CollectionRun 상태

```
CREATED → PLANNING → RUNNING → FINALIZING → COMPLETED
                           ↘         ↘
                            → FAILED   → CANCELED
```

| 상태 | 설명 | 전이 조건 |
|------|------|----------|
| **CREATED** | Run 생성됨 | 초기 상태 |
| **PLANNING** | Job 목록 생성 중 | Orchestrator가 Job 계획 시작 |
| **RUNNING** | Job 실행 중 | 모든 Job 생성 완료 후 |
| **FINALIZING** | 완료 처리 중 | 모든 Job 완료 (성공/실패) |
| **COMPLETED** | 정상 완료 | 통계 계산 성공 |
| **FAILED** | 실패 | 복구 불가능한 에러 발생 |
| **CANCELED** | 취소됨 | 사용자/관리자 취소 |

### 6.2. PageJob 상태 (V3 - 간소화)

```
READY → ENQUEUED → RUNNING → SUCCEEDED
                        ↘
                         → FAILED
```

| 상태 | 설명 | 전이 조건 |
|------|------|----------|
| **READY** | 실행 준비 완료 | 초기 상태 또는 재스케줄 |
| **ENQUEUED** | Redis Stream에 dispatch됨 | Stream에 XADD 완료 |
| **RUNNING** | Worker가 처리 중 | Worker가 XREADGROUP으로 수신 |
| **SUCCEEDED** | 성공 | API 호출 + MongoDB 저장 완료 |
| **FAILED** | 영구 실패 | 재시도 횟수 초과 또는 복구 불가 에러 |

> **V3 변경사항**: 
> - `RETRYABLE_FAILED` 상태 제거 → 재시도는 READY + delayed queue로 처리
> - Lease 기반 복구 제거 → Redis Streams XAUTOCLAIM으로 대체

### 6.3. 재시도 흐름 (V3)

```
실패 발생
    ↓
canRetry()?  ──No──→ FAILED (영구)
    │
   Yes
    ↓
job.markReady()
job.setNotBefore(backoffTime)
    ↓
XACK (현재 메시지 완료)
    ↓
dispatchDelayed(jobId, executeAt)
    ↓
DelayedJobScheduler가 시간 도래 시
    ↓
Lua: ZPOPMIN + XADD (atomic)
    ↓
Worker가 다시 처리
```

---

## 7. Repository SQL 패턴

### 7.1. PageJobRepository (V3 - Streams 기반)

```java
public interface PageJobRepository extends JpaRepository<PageJob, Long> {
    
    /**
     * Job 상태를 RUNNING으로 변경
     * 
     * V3 변경사항:
     * - Lease 컬럼 제거 (Streams XAUTOCLAIM이 복구 담당)
     * - workerId 제거 (Streams Consumer ID로 추적)
     */
    @Modifying
    @Query("""
        UPDATE PageJob j 
        SET j.status = 'RUNNING', 
            j.startedAt = :startedAt,
            j.attemptCount = j.attemptCount + 1
        WHERE j.id = :jobId 
          AND j.status = 'ENQUEUED'
        """)
    int markRunning(
        @Param("jobId") Long jobId,
        @Param("startedAt") LocalDateTime startedAt
    );
    
    /**
     * 진행 중인 Job 수 (Run 완료 평가용)
     * SUCCEEDED, FAILED가 아닌 모든 상태는 "진행 중"
     */
    @Query("""
        SELECT COUNT(j) FROM PageJob j 
        WHERE j.run.id = :runId 
          AND j.status NOT IN ('SUCCEEDED', 'FAILED')
        """)
    long countPendingJobs(@Param("runId") Long runId);
    
    /**
     * 상태별 Job 수 조회
     */
    long countByRunIdAndStatus(Long runId, JobStatus status);
    
    /**
     * READY 상태 Job 조회 (dispatch 대상)
     */
    @Query("""
        SELECT j FROM PageJob j 
        WHERE j.run.id = :runId 
          AND j.status = 'READY'
        ORDER BY j.createdAt ASC
        """)
    List<PageJob> findReadyJobs(@Param("runId") Long runId);
    
    /**
     * 중복 Job 존재 여부 확인
     */
    @Query("""
        SELECT COUNT(j) > 0 FROM PageJob j 
        WHERE j.run.id = :runId 
          AND j.jobType = :jobType 
          AND j.resourceKey = :resourceKey 
          AND (j.pageKey = :pageKey OR (j.pageKey IS NULL AND :pageKey IS NULL))
        """)
    boolean existsByRunAndTypeAndResource(
        @Param("runId") Long runId,
        @Param("jobType") JobType jobType,
        @Param("resourceKey") String resourceKey,
        @Param("pageKey") String pageKey
    );
    
    /**
     * Stuck Job 복구 후 상태 리셋 (XAUTOCLAIM으로 복구된 Job용)
     */
    @Modifying
    @Query("""
        UPDATE PageJob j 
        SET j.status = 'ENQUEUED'
        WHERE j.id = :jobId 
          AND j.status = 'RUNNING'
        """)
    int resetToEnqueued(@Param("jobId") Long jobId);
}
```

### 7.2. CollectionRunRepository

```java
public interface CollectionRunRepository extends JpaRepository<CollectionRun, Long> {
    
    /**
     * 활성 Run 존재 여부 (중복 실행 방지)
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM CollectionRun r 
        WHERE r.userId = :userId 
          AND r.status IN ('CREATED', 'PLANNING', 'RUNNING', 'FINALIZING')
        """)
    boolean existsActiveRun(@Param("userId") Long userId);
    
    /**
     * 사용자의 가장 최근 완료된 Run 조회
     */
    @Query("""
        SELECT r FROM CollectionRun r 
        WHERE r.userId = :userId 
          AND r.status = 'COMPLETED'
        ORDER BY r.completedAt DESC
        LIMIT 1
        """)
    Optional<CollectionRun> findLatestCompletedRun(@Param("userId") Long userId);
    
    /**
     * 상태별 Run 수 (모니터링용)
     */
    @Query("""
        SELECT r.status, COUNT(r) 
        FROM CollectionRun r 
        WHERE r.createdAt > :since 
        GROUP BY r.status
        """)
    List<Object[]> countByStatusSince(@Param("since") LocalDateTime since);
}
```

### 7.3. CollectionCursorRepository

```java
public interface CollectionCursorRepository extends JpaRepository<CollectionCursor, Long> {
    
    /**
     * 특정 리소스의 커서 조회 (증분 수집용)
     */
    @Query("""
        SELECT c FROM CollectionCursor c 
        WHERE c.userId = :userId 
          AND c.resourceType = :resourceType 
          AND c.resourceKey = :resourceKey
        """)
    Optional<CollectionCursor> findByUserAndResource(
        @Param("userId") Long userId,
        @Param("resourceType") String resourceType,
        @Param("resourceKey") String resourceKey
    );
    
    /**
     * 커서 Upsert (증분 수집 위치 저장)
     */
    @Modifying
    @Query(value = """
        INSERT INTO collection_cursors (user_id, resource_type, resource_key, last_cursor, last_timestamp, last_run_id, updated_at)
        VALUES (:userId, :resourceType, :resourceKey, :lastCursor, :lastTimestamp, :runId, NOW())
        ON DUPLICATE KEY UPDATE 
            last_cursor = :lastCursor, 
            last_timestamp = :lastTimestamp, 
            last_run_id = :runId, 
            updated_at = NOW()
        """, nativeQuery = true)
    void upsertCursor(
        @Param("userId") Long userId,
        @Param("resourceType") String resourceType,
        @Param("resourceKey") String resourceKey,
        @Param("lastCursor") String lastCursor,
        @Param("lastTimestamp") LocalDateTime lastTimestamp,
        @Param("runId") Long runId
    );
}

---

## 8. 마이그레이션 전략 (V3)

### 8.1. Phase 1: 인프라 준비 (1-2일)

**MySQL 테이블 생성**
```sql
-- 순서 중요: 외래키 의존성 고려
1. collection_runs
2. page_jobs (FK: collection_runs) -- V3: worker_id, lease_until 컬럼 없음
3. collection_cursors
```

**Lua 스크립트 준비**
```
resources/lua/
├── rate_limit_reserve.lua       # Rate Limit 원자적 예약
├── rate_limit_update.lua        # API 응답 후 업데이트
├── delayed_promote.lua          # Delayed → Stream 이동
└── scheduler_wakeup.lua         # Delayed 추가 + Scheduler 알림
```

**새로운 컴포넌트 구현**
1. Entity 클래스: `CollectionRun`, `PageJob`, `CollectionCursor`
2. Repository 인터페이스 (Section 7 참조)
3. `RateLimitReservation` - Lua 기반 atomic 예약
4. `JobDispatcher` - Redis Streams 기반
5. `DelayedJobScheduler` - Sleep-until-next-due (폴링 제로)

**Redis 키 설계 (V3)**
```
github:jobs:stream              # Stream - XREADGROUP BLOCK
github:jobs:delayed             # ZSet - Score = executeAt (ms)
github:ratelimit:{tokenHash}    # Hash - {remaining, resetAt}
github:scheduler:notify         # Pub/Sub - Scheduler wake-up
github:scheduler:lock           # String - Leader election
github:stats:retry              # ZSet - 통계 재시도 큐
```

### 8.2. Phase 2: Core Implementation (2-3일)

1. **Lua 스크립트 등록**
   ```java
   @Bean
   public RedisScript<List<Long>> rateLimitReserveScript() {
       return RedisScript.of(new ClassPathResource("lua/rate_limit_reserve.lua"));
   }
   ```

2. **Consumer Group 초기화**
   ```java
   @PostConstruct
   public void initConsumerGroup() {
       try {
           redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
       } catch (RedisSystemException e) {
           // Group already exists - OK
       }
   }
   ```

3. **Worker 구현** (Redis Streams)
   - `XREADGROUP BLOCK 0` 무한 대기
   - `XACK` 성공 시 제거
   - `XAUTOCLAIM` 시작 시 stuck job 복구

4. **Event-driven 통계 연동**
   - `RunCompletionEvaluator` - Job 완료 시 동기 호출
   - `StatisticsEventListener` - `@TransactionalEventListener`
   - `StatisticsRetryQueue` - 실패 시 재시도

5. **DelayedJobScheduler**
   - Leader election으로 단일 인스턴스만 실행
   - Sleep-until-next-due (폴링 없음)
   - Pub/Sub 리스너로 새 Job 알림 수신

### 8.3. Phase 3: Shadow Mode (2-3일)

**병행 실행 전략**
```java
@EventListener
public void onUserSignup(UserSignupEvent event) {
    // 기존 경로 (AS-IS)
    existingCollectionService.startCollection(event.getUserId());
    
    // 신규 경로 (TO-BE) - Feature flag로 제어
    if (featureFlags.isEnabled("new-collection-system-v3")) {
        newOrchestrator.startCollection(event.getUserId(), ...);
    }
}
```

**검증 항목**
| 항목 | 검증 방법 |
|------|----------|
| Job 생성 수 일치 | 로그 비교 |
| MongoDB 저장 데이터 일치 | Document count 비교 |
| 통계 계산 결과 일치 | User statistics 비교 |
| 실행 시간 | 메트릭 비교 |
| 에러율 | 로그 분석 |
| **폴링 제로 확인** | CPU 사용량, 스레드 덤프 |
| **XAUTOCLAIM 동작** | 의도적 Worker kill 후 복구 확인 |

### 8.4. Phase 4: Cutover (1일)

**전환 순서**
1. Feature flag로 기존 경로 비활성화
2. 기존 Redis 큐 drain 대기 (진행 중인 Job 완료)
3. 신규 경로 활성화
4. Consumer Group 상태 모니터링
5. 집중 모니터링 (1시간)

**Rollback 계획**
```yaml
조건: 에러율 > 5% 또는 처리 시간 2x 증가
행동:
  1. Feature flag 롤백 (신규 → 기존)
  2. Stream pending 메시지 처리 (XPENDING 확인 후 XACK 또는 재처리)
  3. 원인 분석 후 재시도
```

### 8.5. Phase 5: Cleanup (1일)

**제거 대상 (기존 코드)**
```
- BatchScheduler                    # 배치 스케줄러
- DataCollectionJobConfig           # Spring Batch 설정
- CollectionCompletionTracker       # 인메모리 카운터
- CollectionJobProducer (기존)      # Redis ZSet 기반
- GithubCollectionWorker (기존)     # 1초 폴링 Worker
- RunCompletionChecker (기존)       # 10초 폴링
- OutboxPublisher (기존)            # 1초 폴링
- LeaseManager (기존)               # V3에서 제거 - XAUTOCLAIM 대체
- Reaper (기존)                     # V3에서 제거 - XAUTOCLAIM 대체
```

**제거 대상 (Redis 키)**
```
- github:collection:processing:*    # 인메모리 Set 백업
- github:collection:jobs:*          # 기존 Job 큐
- github:collection:counter:*       # 기존 카운터
- github:jobs:ready                 # V2 BLPOP 큐 (V3는 Stream 사용)
```

**유지 대상**
```
- MongoDB collections               # 데이터 보존
- collection_cursors 테이블         # 증분 수집 위치
- github:jobs:stream               # V3 Stream
- github:jobs:delayed              # V3 Delayed ZSet
```

---

## 9. 모니터링 및 알림 (V3)

### 9.1. 핵심 메트릭

| 메트릭 | 설명 | 알림 임계값 |
|--------|------|------------|
| `collection_runs_active` | 활성 Run 수 | > 100 |
| `page_jobs_pending` | 대기 중인 Job 수 | > 1000 |
| `page_jobs_failed_rate` | 실패율 | > 10% |
| `job_processing_time_p99` | 처리 시간 99퍼센타일 | > 30초 |
| `delayed_queue_size` | Delayed Queue 크기 | > 500 |
| `stream_pending_count` | **Stream Pending 메시지 수** | > 100 |
| `stream_consumer_lag` | **Consumer 처리 지연** | > 50 |
| `rate_limit_tokens_blocked` | Rate Limit 상태인 토큰 수 | > 10 |
| `xautoclaim_recovered` | **XAUTOCLAIM으로 복구된 Job 수** | > 10/분 |
| `stats_retry_queue_size` | 통계 재시도 큐 크기 | > 20 |
| `scheduler_sleep_duration_avg` | **Scheduler 평균 sleep 시간** | 모니터링용 |

> **V3 변경**: `blpop_timeout_rate`, `lease_expired_recovered` 제거 → Stream 기반 메트릭 추가

### 9.2. Redis 모니터링 쿼리 (V3)

```bash
# Stream 정보 조회
redis-cli XINFO STREAM github:jobs:stream

# Consumer Group 상태
redis-cli XINFO GROUPS github:jobs:stream

# Pending 메시지 수 (처리 중인 Job)
redis-cli XPENDING github:jobs:stream workers

# 특정 Consumer의 Pending 상태
redis-cli XPENDING github:jobs:stream workers - + 10 consumer-1

# Delayed Queue 크기
redis-cli ZCARD github:jobs:delayed

# Delayed Queue에서 곧 실행될 Job 수 (1분 이내)
redis-cli ZCOUNT github:jobs:delayed 0 $(expr $(date +%s) \* 1000 + 60000)

# 가장 빠른 Delayed Job 실행 시각
redis-cli ZRANGE github:jobs:delayed 0 0 WITHSCORES

# Rate Limited 토큰 수
redis-cli KEYS "github:ratelimit:*" | wc -l

# 특정 토큰의 Rate Limit 상태
redis-cli HGETALL github:ratelimit:{token_hash}

# Scheduler Lock 상태
redis-cli GET github:scheduler:lock

# 통계 재시도 대기 수
redis-cli ZCARD github:stats:retry
```

### 9.3. MySQL 대시보드 쿼리 (V3)

```sql
-- 활성 Run 현황
SELECT status, COUNT(*) as count 
FROM collection_runs 
WHERE created_at > NOW() - INTERVAL 24 HOUR 
GROUP BY status;

-- Job 상태별 현황 (진행 중인 Run만)
SELECT j.status, COUNT(*) as count 
FROM page_jobs j 
JOIN collection_runs r ON j.run_id = r.id 
WHERE r.status = 'RUNNING' 
GROUP BY j.status;

-- 실패율 높은 Job Type
SELECT job_type, 
       COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed,
       COUNT(*) as total,
       COUNT(CASE WHEN status = 'FAILED' THEN 1 END) * 100.0 / COUNT(*) as fail_rate
FROM page_jobs 
WHERE created_at > NOW() - INTERVAL 1 HOUR 
GROUP BY job_type 
ORDER BY fail_rate DESC;

-- V3: Stuck Job 의심 (RUNNING 상태가 오래된 경우)
-- ※ 정상적으로는 XAUTOCLAIM이 복구하므로 이 쿼리 결과가 있으면 이상 상황
SELECT j.id, j.job_type, j.started_at,
       TIMESTAMPDIFF(MINUTE, j.started_at, NOW()) as running_minutes
FROM page_jobs j
WHERE j.status = 'RUNNING'
  AND j.started_at < NOW() - INTERVAL 10 MINUTE;

-- Rate Limit으로 대기 중인 Job
SELECT j.id, j.job_type, j.not_before, r.github_login
FROM page_jobs j
JOIN collection_runs r ON j.run_id = r.id
WHERE j.status = 'READY'
  AND j.not_before > NOW()
ORDER BY j.not_before ASC;

-- 평균 Job 처리 시간 (성공한 Job만)
SELECT job_type,
       AVG(TIMESTAMPDIFF(SECOND, started_at, completed_at)) as avg_seconds,
       MAX(TIMESTAMPDIFF(SECOND, started_at, completed_at)) as max_seconds
FROM page_jobs
WHERE status = 'SUCCEEDED'
  AND completed_at > NOW() - INTERVAL 1 HOUR
GROUP BY job_type;

-- 재시도 횟수 분포
SELECT attempt_count, COUNT(*) as count
FROM page_jobs
WHERE completed_at > NOW() - INTERVAL 1 HOUR
GROUP BY attempt_count
ORDER BY attempt_count;
```

### 9.4. Grafana Alert Rules (V3)

```yaml
# Stream Pending 적체
- alert: StreamPendingBacklog
  expr: stream_pending_count > 100
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "{{ $value }} messages pending in stream. Check worker health."

# XAUTOCLAIM 빈발 (Worker 문제 신호)
- alert: FrequentXautoclaim
  expr: rate(xautoclaim_recovered[5m]) > 0.1
  for: 10m
  labels:
    severity: warning
  annotations:
    summary: "High XAUTOCLAIM rate detected. Workers may be crashing."

# Rate Limit 토큰 과다
- alert: TooManyRateLimitedTokens
  expr: rate_limit_tokens_blocked > 10
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "{{ $value }} tokens are rate limited"

# Delayed Queue 적체
- alert: DelayedQueueBacklog
  expr: delayed_queue_size > 500
  for: 10m
  labels:
    severity: warning
  annotations:
    summary: "Delayed queue has {{ $value }} jobs waiting"

# Scheduler 장애 (Leader lock 없음)
- alert: SchedulerNotRunning
  expr: scheduler_leader_active == 0
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "DelayedJobScheduler is not running. Delayed jobs will not be promoted."

# 통계 계산 실패 누적
- alert: StatsRetryQueueGrowing
  expr: stats_retry_queue_size > 20
  for: 15m
  labels:
    severity: warning
  annotations:
    summary: "Statistics retry queue has {{ $value }} pending retries"
```

### 9.5. 로그 패턴 (V3)

```java
// 성공 로그
log.info("Job {} completed. Type: {}, Duration: {}ms, Attempt: {}", 
    jobId, jobType, duration, attemptCount);

// Rate Limit 로그
log.info("Job {} rescheduled for rate limit. Token: {}, WaitMs: {}", 
    jobId, tokenHash.substring(0, 8), waitMs);

// XAUTOCLAIM 복구 로그
log.warn("Recovered stuck job via XAUTOCLAIM. Job: {}, IdleTime: {}ms", 
    jobId, idleTimeMs);

// Run 완료 로그
log.info("Run {} completed. User: {}, Succeeded: {}, Failed: {}, Duration: {}s",
    runId, githubLogin, succeeded, failed, durationSeconds);

// Scheduler 로그
log.debug("Scheduler sleeping until {}. Next job: {}", executeAt, jobId);
log.info("Promoted {} jobs from delayed to stream", promotedCount);

// 통계 실패 로그
log.error("Statistics calculation failed for run {}. Reason: {}. Queued for retry.",
    runId, errorMessage);
```

---

## 10. 결론 (V3)

### 10.1. 해결되는 문제

| 기존 문제 | V3 해결 방법 |
|-----------|-------------|
| Batch/Queue 이중 실행 경로 | 단일 Orchestrator 진입점 |
| In-memory 상태 유실 (서버 재시작 시) | MySQL 기반 상태 관리 (Source of Truth) |
| Counter Race Condition | DB `countPendingJobs()` 쿼리 기반 완료 평가 |
| Zombie Job (처리 중 Worker 죽음) | **Redis Streams XAUTOCLAIM 자동 복구** |
| 중복 통계 트리거 (3곳에서 호출) | `@TransactionalEventListener` 단일 트리거 |
| Job 중복 생성 | DB Unique Constraint (`uk_run_type_resource_page`) |
| Rate Limit 분산 처리 (Race 있음) | **Lua Atomic Reservation (Race-free)** |
| 1초 폴링 (Worker, Reaper, Outbox) | **완전 제거 → XREADGROUP BLOCK, Sleep-until** |
| 10초 폴링 (Run 완료 평가) | Job 완료 시 동기 호출로 즉시 평가 |
| Delayed Job 처리 폴링 | **Sleep-until-next-due + Pub/Sub (폴링 제로)** |

### 10.2. V2 → V3 설계 변경 요약

| 항목 | V2 | V3 |
|------|-----|-----|
| **Job Queue** | Redis List + BLPOP | **Redis Streams + XREADGROUP** |
| **Crash 복구** | Lease + Reaper 폴링 | **XAUTOCLAIM (Stream 내장)** |
| **Rate Limit** | `canProceed()` check | **Lua Atomic Reservation** |
| **Delayed 처리** | 1초 폴링 | **Sleep-until-next-due** |
| **Worker 대기** | BLPOP (crash 시 유실) | **XREADGROUP (pending 보존)** |
| **폴링 컴포넌트** | 1개 (DelayedQueuePromoter) | **0개 (완전 event-driven)** |
| **DB 스키마** | worker_id, lease_until 필요 | **제거 (Streams가 처리)** |

### 10.3. 예상 효과

**안정성**
- 서버 재시작/크래시에도 상태 보존 (MySQL + Streams pending)
- XAUTOCLAIM으로 Zombie Job 자동 복구
- Lua atomic 연산으로 Race condition 원천 차단

**성능**
- **폴링 완전 제거** → CPU 0% (대기 시)
- Event-driven으로 지연 시간 최소화
- Sleep-until-next-due로 Delayed Job 정밀 처리

**확장성**
- Worker 수평 확장 용이 (Consumer Group 자동 분산)
- MySQL/Redis 분리로 독립적 스케일링

**관측성**
- SQL 쿼리로 모든 상태 조회 가능
- Stream XINFO/XPENDING으로 실시간 처리 현황 파악
- Job 단위 상세 로깅 및 메트릭

**유지보수성**
- 단일 실행 경로로 코드 복잡도 감소
- **Lease/Reaper 코드 제거** → 코드량 감소
- 명확한 컴포넌트 역할 분리

### 10.4. 구현 우선순위 (V3)

| 우선순위 | 컴포넌트 | 설명 |
|----------|----------|------|
| **P0** | MySQL 테이블 | `collection_runs`, `page_jobs` (V3 스키마), `collection_cursors` |
| **P0** | Lua 스크립트 | Rate Limit, Delayed Promotion, Scheduler Wake-up |
| **P0** | Entity/Repository | JPA 매핑 및 V3 쿼리 |
| **P0** | `RateLimitReservation` | Lua 기반 atomic 예약 |
| **P0** | `JobDispatcher` | Redis Streams 기반 |
| **P0** | `CollectionOrchestrator` | 단일 진입점 |
| **P0** | `CollectionWorker` | Streams 기반 (XREADGROUP, XAUTOCLAIM) |
| **P1** | `DelayedJobScheduler` | Sleep-until-next-due + Pub/Sub |
| **P1** | `RunCompletionEvaluator` | Run 완료 평가 |
| **P1** | `StatisticsEventListener` | 통계 트리거 |
| **P2** | 모니터링 대시보드 | Grafana + Stream 메트릭 |
| **P2** | 알림 설정 | Alert rules (V3) |
| **P3** | 관리자 API | 수동 재시도, Run 취소 등 |

### 10.5. 리스크 및 완화 방안 (V3)

| 리스크 | 완화 방안 |
|--------|----------|
| Redis Streams 학습 곡선 | 충분한 테스트 + 문서화 |
| Lua 스크립트 디버깅 어려움 | 로컬 Redis에서 단위 테스트 |
| XAUTOCLAIM 타이밍 미스 | minIdleTime 적절히 설정 (5분) |
| Scheduler Leader 경합 | Lock TTL 조정 + 로깅 강화 |
| `@TransactionalEventListener` 실패 | `StatisticsRetryQueue`로 재시도 |
| MySQL 부하 증가 | 인덱스 최적화 + 읽기 복제본 |
| Redis 메모리 증가 | Stream MAXLEN/MINID 트리밍 |

---

## 11. 엣지 케이스 분석 (V3)

### 11.1. Redis Streams Graceful Shutdown

**문제**: 서버 종료 시 XREADGROUP 대기 중인 Worker가 있으면?

**V3 해결** (BLPOP보다 간단):
```java
@Component
public class WorkerShutdownHandler {
    
    private final List<CollectionWorker> workers = new ArrayList<>();
    private volatile boolean shuttingDown = false;
    
    @PreDestroy
    public void onShutdown() {
        shuttingDown = true;
        
        // 1. Worker 종료 플래그 설정 (루프 탈출)
        workers.forEach(w -> w.requestShutdown());
        
        // 2. 진행 중인 Job 완료 대기 (최대 30초)
        // ★ V3: XACK 전까지 메시지는 pending 상태로 보존됨
        // 강제 종료되어도 XAUTOCLAIM이 나중에 복구
        workers.forEach(w -> {
            try {
                w.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        log.info("All workers gracefully shut down");
    }
}
```

**Worker 루프 (V3)**:
```java
public void startWorkerLoop() {
    while (!shuttingDown) {
        try {
            // XREADGROUP은 timeout 설정으로 주기적으로 깨어남
            JobMessage message = jobDispatcher.blockingRead(consumerId, Duration.ofSeconds(30));
            
            if (message == null) {
                continue;  // Timeout - shuttingDown 플래그 체크 후 재시도
            }
            
            processJob(message);
            
        } catch (Exception e) {
            log.error("Worker loop error", e);
        }
    }
    log.info("Worker {} exiting gracefully", consumerId);
}
```

> **V3 장점**: Shutdown 중 처리 못한 Job은 pending 상태로 남아 XAUTOCLAIM이 복구

### 11.2. XAUTOCLAIM 동작 시나리오

**시나리오**: Worker A가 Job을 가져간 후 크래시

```
1. Worker A: XREADGROUP → Job 123 수신 (pending 상태)
2. Worker A: 크래시 (XACK 전)
3. 5분 경과...
4. Worker B 시작: claimStuckJobs(minIdleTime=5분) 호출
5. Worker B: XAUTOCLAIM → Job 123을 자신에게 이전
6. Worker B: 정상 처리 후 XACK
```

**구현**:
```java
public void recoverStuckJobs() {
    // minIdleTime 이상 pending 상태인 메시지 claim
    List<JobMessage> stuckJobs = jobDispatcher.claimStuckJobs(
        consumerId, 
        Duration.ofMinutes(5)  // STUCK_THRESHOLD
    );
    
    for (JobMessage msg : stuckJobs) {
        log.warn("Recovered stuck job via XAUTOCLAIM. Job: {}, IdleTime: {}ms", 
            msg.jobId(), msg.idleTimeMs());
        
        // DB 상태 리셋 (RUNNING → ENQUEUED)
        jobRepository.resetToEnqueued(msg.jobId());
        
        // 다시 처리
        processJob(msg);
    }
}
```

### 11.3. @TransactionalEventListener 실패

**문제**: 통계 계산이 실패하면 이벤트가 유실됨

**해결**: `StatisticsRetryQueue` (동일)
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onRunCompleted(RunCompletedEvent event) {
    try {
        statisticsService.calculate(event.run());
    } catch (Exception e) {
        // 실패 시 재시도 큐에 등록 (Redis ZSet)
        retryQueue.enqueue(event.run().getId());
        log.error("Statistics failed, queued for retry: {}", e.getMessage());
    }
}
```

**보완**: 재시도 횟수 제한 + Dead Letter 처리
```java
public void enqueue(Long runId) {
    int retryCount = getRetryCount(runId);
    
    if (retryCount >= MAX_RETRIES) {
        // Dead Letter로 이동 (수동 처리 필요)
        moveToDeadLetter(runId);
        alertService.send("Statistics failed permanently for run " + runId);
        return;
    }
    
    long backoffMs = (long) Math.pow(2, retryCount) * 60_000;  // 1분, 2분, 4분...
    long executeAt = System.currentTimeMillis() + backoffMs;
    
    redisTemplate.opsForZSet().add(RETRY_QUEUE_KEY, runId.toString(), executeAt);
    incrementRetryCount(runId);
}
```

### 11.4. DelayedJobScheduler Leader 경합

**문제**: 여러 서버에서 Scheduler 경합 시 어떻게 되나?

**해결**: Redis Lock + TTL
```java
private void schedulerLoop() {
    while (running) {
        // Leader lock 획득 시도
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(LOCK_KEY, consumerId, LOCK_TTL);  // 30초 TTL
        
        if (Boolean.FALSE.equals(acquired)) {
            // 다른 인스턴스가 Leader - 잠시 대기 후 재시도
            sleep(Duration.ofSeconds(5));
            continue;
        }
        
        try {
            runAsLeader();
        } finally {
            // Lock 해제 (자신이 획득한 경우만)
            String currentHolder = redisTemplate.opsForValue().get(LOCK_KEY);
            if (consumerId.equals(currentHolder)) {
                redisTemplate.delete(LOCK_KEY);
            }
        }
    }
}
```

**리더 유지 중 Lock 연장**:
```java
private void runAsLeader() {
    while (running) {
        // 주기적으로 Lock TTL 연장
        redisTemplate.expire(LOCK_KEY, LOCK_TTL);
        
        // ... 실제 스케줄링 로직 ...
    }
}
```

### 11.5. Stream Trimming 전략

**문제**: Stream이 무한히 커지면?

**해결 1**: MAXLEN (개수 기반)
```java
// Job dispatch 시 자동 trim
redisTemplate.opsForStream().add(
    StreamRecords.newRecord()
        .ofMap(Map.of("jobId", jobId.toString()))
        .withStreamKey(STREAM_KEY),
    RedisStreamCommands.XAddOptions.makeNoLimit()
        .maxlen(10000)  // 최대 10,000개 유지
);
```

**해결 2**: MINID (시간 기반) - 권장
```java
// 주기적 정리 (예: 1시간마다)
@Scheduled(fixedRate = 3600000)
public void trimStream() {
    // 24시간 이전 메시지 삭제
    long minId = System.currentTimeMillis() - Duration.ofDays(1).toMillis();
    redisTemplate.opsForStream().trim(STREAM_KEY, minId);
}
```

### 11.6. Lua 스크립트 디버깅

**문제**: Lua 스크립트 오류 시 디버깅 어려움

**해결 1**: 로컬 테스트
```bash
# redis-cli에서 직접 테스트
redis-cli EVAL "$(cat rate_limit_reserve.lua)" 1 github:ratelimit:test 1 100 1705380000000
```

**해결 2**: 스크립트 로깅 (개발 환경)
```lua
-- 디버그용 로그 (운영에서는 제거)
redis.log(redis.LOG_WARNING, "Reserve called: key=" .. KEYS[1] .. ", permits=" .. ARGV[1])
```

**해결 3**: 단위 테스트
```java
@Test
void reserveScript_shouldDecrementRemaining() {
    // given
    redisTemplate.opsForHash().putAll("github:ratelimit:test", Map.of(
        "remaining", "5000",
        "resetAt", String.valueOf(System.currentTimeMillis() + 3600000)
    ));
    
    // when
    List<Long> result = redisTemplate.execute(
        reserveScript,
        List.of("github:ratelimit:test"),
        "1", "100", String.valueOf(System.currentTimeMillis())
    );
    
    // then
    assertThat(result.get(0)).isEqualTo(1);  // allowed
    assertThat(result.get(2)).isEqualTo(4999);  // remaining
}
```

### 11.7. Job 생성 중 서버 크래시

**문제**: Orchestrator가 Job 일부만 생성하고 죽으면?

**현재 설계**: Run 상태가 `PLANNING`에서 멈춤

**해결**: Startup Recovery
```java
@PostConstruct
public void recoverIncompleteRuns() {
    // PLANNING 상태로 5분 이상 멈춘 Run 조회
    List<CollectionRun> stuckRuns = runRepository
        .findByStatusAndCreatedAtBefore(RunStatus.PLANNING, 
            LocalDateTime.now().minusMinutes(5));
    
    for (CollectionRun run : stuckRuns) {
        log.warn("Recovering stuck PLANNING run: {}", run.getId());
        
        // 기존 Job 삭제 후 다시 계획
        jobRepository.deleteByRunId(run.getId());
        run.setStatus(RunStatus.CREATED);
        runRepository.save(run);
        
        // 다시 시작
        orchestrator.resumeCollection(run);
    }
}
```

### 11.8. MongoDB 쓰기 실패

**문제**: GitHub API 성공했는데 MongoDB 저장 실패

**현재 설계**: Job이 재시도 대상이 됨

**개선안**: API 결과 임시 캐싱
```java
private void processJob(JobMessage message) {
    Long jobId = message.jobId();
    
    // Redis에서 캐시된 결과 확인
    String cached = redisTemplate.opsForValue()
        .get("github:pending-write:" + jobId);
    
    ApiResponse response;
    if (cached != null) {
        response = objectMapper.readValue(cached, ApiResponse.class);
        redisTemplate.delete("github:pending-write:" + jobId);
    } else {
        response = githubClient.fetch(job.toApiRequest());
    }
    
    try {
        mongoWriter.write(response.getData());
    } catch (MongoException e) {
        // MongoDB 실패 → Redis에 임시 저장 (1시간 TTL)
        redisTemplate.opsForValue().set(
            "github:pending-write:" + jobId,
            objectMapper.writeValueAsString(response),
            Duration.ofHours(1)
        );
        throw new RecoverableException("MongoDB write failed", e);
    }
}
```

### 11.9. Consumer Group 초기화 실패

**문제**: Consumer Group이 없으면 XREADGROUP 실패

**해결**: @PostConstruct에서 안전하게 생성
```java
@PostConstruct
public void initConsumerGroup() {
    try {
        // Stream이 없으면 빈 Stream과 함께 Group 생성
        redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
        log.info("Consumer group '{}' created for stream '{}'", GROUP_NAME, STREAM_KEY);
    } catch (RedisSystemException e) {
        if (e.getCause() instanceof RedisCommandExecutionException) {
            String message = e.getCause().getMessage();
            if (message.contains("BUSYGROUP")) {
                // Group already exists - OK
                log.debug("Consumer group '{}' already exists", GROUP_NAME);
            } else {
                throw e;
            }
        } else {
            throw e;
        }
    }
}
```

---

*문서 작성일: 2025-01-16*  
*버전: 3.0 (V3 - Zero Polling, Lua Atomic, Redis Streams)*  
*작성자: AI Assistant (Oracle 자문 기반)*
