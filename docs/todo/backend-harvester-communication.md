# Backend-Harvester 통신 구현 TODO

## 개요

Backend에서 Harvester로 GitHub 데이터 수집 트리거를 보내는 기능이 필요함.

## 제거된 기능 (구현 필요)

### 1. Backend → Harvester 트리거 발행

**기존 구현 (Redis Stream):**
- `backend/UserSignupEventListener.java` - 회원가입 시 `github:collection:trigger` 스트림에 발행
- Harvester가 Redis Stream을 구독하여 수집 Job 실행

**현재 상태:**
- `UserSignupEventListener`가 JdbcTemplate NOTIFY로 변경됨
- Harvester 측 Listener가 제거됨 (R2DBC 문제로)

**구현 필요:**
- [ ] Harvester에서 PostgreSQL LISTEN 구현 (R2DBC 없이)
- [ ] 또는 다른 통신 방식 선택

### 2. 제거된 Harvester 파일들

| 파일 | 역할 | 비고 |
|------|------|------|
| `trigger/CollectionTriggerListener.java` | Redis Stream 구독 → Job 실행 | R2DBC로 재작성 시도했으나 실패 |
| `trigger/PendingMessageRecovery.java` | 앱 시작 시 미처리 메시지 복구 | Redis Stream 의존 |
| `trigger/ChallengeCheckPublisher.java` | Score 계산 후 Backend에 알림 | 공유 모듈로 불필요해짐 |
| `config/RedisStreamConfig.java` | Redis Stream 설정 | 제거됨 |

### 3. 제거된 Backend 파일들

| 파일 | 역할 | 비고 |
|------|------|------|
| `challenge/listener/ChallengeCheckListener.java` | Harvester로부터 Score 업데이트 알림 수신 | 공유 모듈로 불필요해짐 |
| `global/config/redis/RedisStreamConfig.java` | Redis Stream 설정 | 제거됨 |

## 대안 옵션

### Option 1: JDBC Polling (단순)
- Harvester가 주기적으로 `trigger_queue` 테이블 폴링
- 장점: 단순, JDBC만 사용
- 단점: 지연 발생 (polling interval)

### Option 2: pgjdbc-ng (Async LISTEN)
- `pgjdbc-ng` 드라이버 사용 (R2DBC 아님)
- 장점: 진정한 이벤트 드리븐
- 단점: 추가 의존성

### Option 3: HTTP Webhook
- Backend가 Harvester에 HTTP 호출
- 장점: 단순, 표준적
- 단점: Harvester가 웹서버 필요 (현재 `web-application-type: none`)

### Option 4: 기존 Redis Stream 유지
- Redis Stream 그대로 사용
- 장점: 이미 검증됨
- 단점: Redis 의존성 유지

## 권장사항

**Option 1 (JDBC Polling)** 권장:
1. 구현 단순
2. 추가 의존성 없음
3. 기존 Spring Batch와 자연스럽게 통합
4. 수집 Job은 어차피 분 단위로 실행해도 충분

## 구현 예시 (Option 1)

```sql
-- 트리거 큐 테이블
CREATE TABLE collection_trigger_queue (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING'
);
```

```java
// Backend: 트리거 발행
@Component
@RequiredArgsConstructor
public class CollectionTriggerPublisher {
    private final JdbcTemplate jdbcTemplate;
    
    public void publish(Long userId) {
        jdbcTemplate.update(
            "INSERT INTO collection_trigger_queue (user_id) VALUES (?)",
            userId
        );
    }
}

// Harvester: 주기적 폴링
@Scheduled(fixedDelay = 5000)
public void pollTriggers() {
    List<Long> userIds = jdbcTemplate.queryForList(
        "UPDATE collection_trigger_queue SET status = 'PROCESSING', processed_at = NOW() " +
        "WHERE status = 'PENDING' RETURNING user_id",
        Long.class
    );
    userIds.forEach(id -> jobLauncher.submit(id, Priority.HIGH));
}
```

## 우선순위

- **높음**: 회원가입 후 GitHub 데이터 수집 트리거
- **중간**: 미처리 메시지 복구 (앱 재시작 시)
- **낮음**: Challenge 체크 알림 (공유 모듈로 불필요)
