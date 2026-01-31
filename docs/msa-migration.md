# KOSP Backend MSA Migration Guide

**Last Updated**: 2026-01-31  
**Migration Status**: ✅ Implementation Complete  
**Services**: api-service, challenge-service, notification-service, harvester

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Service Details](#service-details)
4. [Communication Patterns](#communication-patterns)
5. [Getting Started](#getting-started)
6. [Development Guide](#development-guide)
7. [Testing](#testing)
8. [Deployment](#deployment)
9. [Troubleshooting](#troubleshooting)

---

## Overview

### What Changed

The KOSP Backend has been migrated from a **monolithic Spring Boot application** to a **microservices architecture** using Gradle multi-module and RabbitMQ for event-driven communication.

**Before (Monolith)**:
- Single Spring Boot application handling all concerns
- Redis Stream polling (1s interval) for cross-service events
- Tight coupling between API, challenge evaluation, and notifications

**After (MSA)**:
- 4 independent services with clear responsibilities
- RabbitMQ event bus for asynchronous communication
- Outbox Pattern for transactional messaging
- Zero frontend changes (100% backward compatible)

### Key Benefits

✅ **Separation of Concerns**: Each service has a single responsibility  
✅ **Scalability**: Services can scale independently  
✅ **Reliability**: Manual ACK, Dead Letter Queues, idempotency handling  
✅ **Event-Driven**: RabbitMQ replaces inefficient Redis Stream polling  
✅ **Transaction Safety**: Outbox Pattern ensures no message loss  
✅ **Frontend Compatibility**: Nginx routing preserves all existing endpoints  

---

## Architecture

### Service Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (Unchanged)                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
                    ┌────────────────┐
                    │  Nginx (8080)  │
                    │  Reverse Proxy │
                    └────────────────┘
                             │
              ┌──────────────┼──────────────┐
              │                             │
              ▼                             ▼
     ┌─────────────────┐          ┌──────────────────┐
     │  api-service    │          │ notification-    │
     │  (REST API)     │          │ service (SSE)    │
     │  Port: 8080     │          │ Port: 8081       │
     └────────┬────────┘          └────────┬─────────┘
              │                            │
              │                            │
              ▼                            ▼
     ┌─────────────────────────────────────────────┐
     │            RabbitMQ Event Bus               │
     │  - challenge-evaluation-queue               │
     │  - challenge-completed-queue                │
     │  - point-changed-queue                      │
     │  + Dead Letter Queues (3)                   │
     └─────────────────┬───────────────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ challenge-       │
              │ service (Worker) │
              │ No HTTP server   │
              └─────────┬────────┘
                        │
                        │
     ┌──────────────────┼──────────────────┐
     │                  │                  │
     ▼                  ▼                  ▼
┌─────────┐      ┌──────────┐      ┌──────────┐
│PostgreSQL│      │ RabbitMQ │      │  Valkey  │
│  18.1   │      │   3-mgmt │      │  (Redis) │
└─────────┘      └──────────┘      └──────────┘
```

### Data Flow Examples

#### Example 1: User Signup → Challenge Evaluation
```
1. Frontend → Nginx → api-service: POST /v1/users/signup
2. api-service: Save user to PostgreSQL
3. api-service: Save ChallengeEvaluationRequest to outbox_messages table
4. OutboxPublisher (scheduled): Publish to RabbitMQ challenge-evaluation-queue
5. challenge-service: Consume from queue → Evaluate challenges
6. challenge-service: Save ChallengeCompletedEvent to outbox_messages
7. OutboxPublisher: Publish to RabbitMQ challenge-completed-queue
8. notification-service: Consume → Send SSE to connected client
```

#### Example 2: SSE Notification Subscription
```
1. Frontend: EventSource("/v1/notifications/subscribe/123")
2. Nginx: Route to notification-service:8081
3. notification-service: Create SseEmitter, store in ConcurrentHashMap
4. Keep-alive: 60-minute timeout with periodic heartbeats
5. On event: notification-service sends SSE message to client
```

---

## Service Details

### 1. api-service

**Purpose**: REST API gateway for all non-notification endpoints  
**Port**: 8080  
**Type**: Web application (Spring Web)  

**Responsibilities**:
- Handle all HTTP REST endpoints (26 controllers)
- User authentication & authorization (JWT)
- Business logic orchestration
- Publish events to RabbitMQ via Outbox Pattern

**Dependencies**:
- PostgreSQL (JPA entities, Flyway migrations)
- RabbitMQ (event publishing via OutboxPublisher)
- Redis/Valkey (JWT blacklist, session management)
- AWS S3, SES (file upload, email)

**Key Files**:
- `api-service/src/main/java/io/swkoreatech/kosp/ApiServiceApplication.java`
- `api-service/src/main/resources/application.yml`
- Controllers: 26 controllers copied from original backend

**Build & Run**:
```bash
./gradlew :api-service:bootJar
java -jar api-service/build/libs/api-service-1.0.0.jar
```

---

### 2. challenge-service

**Purpose**: Background worker for challenge evaluation  
**Port**: None (non-web service)  
**Type**: Worker application (WebApplicationType.NONE)  

**Responsibilities**:
- Consume ChallengeEvaluationRequest from RabbitMQ
- Evaluate user challenges (GitHub contribution analysis)
- Publish ChallengeCompletedEvent and PointChangedEvent
- Idempotency handling with message deduplication

**Dependencies**:
- PostgreSQL (JPA for User, Challenge entities)
- RabbitMQ (listener + publisher)
- MongoDB (GitHub activity data)

**Key Patterns**:
```java
@SpringBootApplication
@EnableScheduling  // For OutboxPublisher
public class ChallengeServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ChallengeServiceApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);  // No HTTP server
        app.run(args);
    }
}
```

**Manual ACK RabbitMQ Listener**:
```java
@RabbitListener(queues = QueueNames.CHALLENGE_EVALUATION, concurrency = "5")
@Transactional
public void handleEvaluationRequest(
        ChallengeEvaluationRequest request,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
        Channel channel) throws IOException {
    
    // Idempotency check
    if (processedMessageRepository.existsByMessageId(request.messageId())) {
        log.info("Duplicate message: {}", request.messageId());
        channel.basicAck(deliveryTag, false);
        return;
    }
    
    try {
        // Business logic
        User user = userRepository.getById(request.userId());
        challengeEvaluator.evaluate(user);
        
        // Mark as processed
        processedMessageRepository.save(
            new ProcessedMessage(request.messageId(), "ChallengeEvaluationRequest")
        );
        
        channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
        log.error("Evaluation failed", e);
        channel.basicNack(deliveryTag, false, false);  // → DLQ
    }
}
```

**Build & Run**:
```bash
./gradlew :challenge-service:bootJar
java -jar challenge-service/build/libs/challenge-service-1.0.0.jar
```

---

### 3. notification-service

**Purpose**: SSE notification broadcasting + RabbitMQ event consumer  
**Port**: 8081  
**Type**: Web application (Spring Web)  

**Responsibilities**:
- Expose `/v1/notifications/subscribe/{userId}` SSE endpoint
- Manage SSE connections (ConcurrentHashMap)
- Consume challenge-completed and point-changed events from RabbitMQ
- Broadcast notifications to connected clients

**SSE Connection Management**:
```java
@Service
public class NotificationService {
    private static final Long SSE_TIMEOUT = 60L * 60 * 1000;  // 60 minutes
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);
        
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        
        sendToClient(emitter, "connect", "SSE 연결 성공");
        return emitter;
    }
    
    public void sendChallengeNotification(ChallengeCompletedEvent event) {
        SseEmitter emitter = emitters.get(event.userId());
        if (emitter == null) {
            log.warn("User {} not connected", event.userId());
            return;
        }
        String message = String.format(
            "챌린지 '%s'를 완료했습니다! (%d점 획득)",
            event.challengeName(), event.points()
        );
        sendToClient(emitter, "challenge-completed", message);
    }
}
```

**RabbitMQ Event Listeners** (2):
- `handleChallengeCompleted()` - Listens to `challenge-completed-queue`
- `handlePointChanged()` - Listens to `point-changed-queue`

**Build & Run**:
```bash
./gradlew :notification-service:bootJar
java -jar notification-service/build/libs/notification-service-1.0.0.jar
```

---

### 4. harvester (Modified)

**Purpose**: GitHub data mining batch job  
**Port**: None (batch worker)  
**Type**: Spring Batch application  

**Changes in MSA**:
- ✅ **Preserved**: JobQueueService (Backend → Harvester via Redis ZSET)
- ✅ **Changed**: ChallengeEvaluationStep now publishes to RabbitMQ (not Redis Stream)
- ✅ **Event**: Publishes `ChallengeEvaluationRequest` with UUID messageId

**Communication Patterns**:
1. **Backend → Harvester**: Redis ZSET (JobQueueService) - UNCHANGED
2. **Harvester → challenge-service**: RabbitMQ (NEW)

**Build & Run**:
```bash
./gradlew :harvester:bootJar
java -jar harvester/build/libs/harvester-1.0.0.jar
```

---

## Communication Patterns

### Pattern 1: Outbox Pattern (Transactional Messaging)

**Problem**: Ensuring database writes and event publishing are atomic.

**Solution**: Outbox table + scheduled publisher.

#### Database Schema
```sql
CREATE TABLE outbox_messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_outbox_status ON outbox_messages(status);
CREATE INDEX idx_outbox_created_at ON outbox_messages(created_at);
```

#### OutboxPublisher
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final OutboxMessageRepository outboxMessageRepository;
    
    @Scheduled(fixedDelay = 5000)  // Every 5 seconds
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> pending = outboxMessageRepository
            .findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        
        for (OutboxMessage message : pending) {
            try {
                String queueName = getQueueName(message.getEventType());
                rabbitTemplate.convertAndSend(queueName, message.getPayload());
                
                message.markAsPublished();
                outboxMessageRepository.save(message);
            } catch (Exception e) {
                log.error("Failed to publish message {}", message.getId(), e);
                message.markAsFailed();
                outboxMessageRepository.save(message);
            }
        }
    }
}
```

**Usage in Service**:
```java
@Transactional
public void someBusinessMethod(User user) {
    // 1. Database write
    userRepository.save(user);
    
    // 2. Save event to outbox (same transaction)
    ChallengeEvaluationRequest event = new ChallengeEvaluationRequest(
        UUID.randomUUID().toString(),
        user.getId()
    );
    outboxMessageRepository.save(new OutboxMessage(
        event.messageId(),
        "ChallengeEvaluationRequest",
        objectMapper.writeValueAsString(event),
        OutboxStatus.PENDING
    ));
    
    // Commit transaction → OutboxPublisher will pick it up
}
```

---

### Pattern 2: Idempotency Handling

**Problem**: RabbitMQ may redeliver messages, causing duplicate processing.

**Solution**: Track processed messageIds in database.

#### Database Schema
```sql
CREATE TABLE processed_messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_id ON processed_messages(message_id);
```

#### Implementation
```java
@RabbitListener(queues = QueueNames.CHALLENGE_EVALUATION)
@Transactional
public void handle(ChallengeEvaluationRequest event, Channel channel, 
                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    // Check if already processed
    if (processedMessageRepository.existsByMessageId(event.messageId())) {
        log.info("Duplicate message: {}", event.messageId());
        channel.basicAck(tag, false);
        return;
    }
    
    try {
        // Process event
        doWork(event);
        
        // Mark as processed
        processedMessageRepository.save(
            new ProcessedMessage(event.messageId(), "ChallengeEvaluationRequest")
        );
        
        channel.basicAck(tag, false);
    } catch (Exception e) {
        log.error("Processing failed", e);
        channel.basicNack(tag, false, false);  // → DLQ
    }
}
```

---

### Pattern 3: Manual ACK with DLQ

**Configuration** (application.yml):
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:admin}
    password: ${RABBITMQ_PASSWORD:admin}
    listener:
      simple:
        acknowledge-mode: manual  # CRITICAL
```

**Queue Configuration** (RabbitMQConfig.java):
```java
@Bean
public Queue challengeEvaluationQueue() {
    return QueueBuilder.durable(QueueNames.CHALLENGE_EVALUATION)
        .withArgument("x-dead-letter-routing-key", QueueNames.CHALLENGE_EVALUATION + ".dlq")
        .build();
}

@Bean
public Queue challengeEvaluationDLQ() {
    return QueueBuilder.durable(QueueNames.CHALLENGE_EVALUATION + ".dlq").build();
}
```

**Listener with Manual ACK**:
```java
@RabbitListener(queues = QueueNames.CHALLENGE_EVALUATION, concurrency = "5")
public void handle(Event event, Channel channel, 
                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    try {
        processEvent(event);
        channel.basicAck(tag, false);  // Success: remove from queue
    } catch (Exception e) {
        log.error("Failed", e);
        channel.basicNack(tag, false, false);  // Failure: send to DLQ
    }
}
```

---

## Getting Started

### Prerequisites

- **Java 17**
- **Gradle 8.x**
- **Docker & Docker Compose**

### Environment Setup

1. **Clone repository**:
```bash
git clone <repository-url>
cd KOSP-Backend
```

2. **Create environment files**:

**`.env.local`** (application secrets):
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=kosp
DB_USERNAME=kosp
DB_PASSWORD=kosp

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=admin

# Redis/Valkey
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# MongoDB
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=kosp
MONGODB_USERNAME=kosp
MONGODB_PASSWORD=kosp

# JWT
JWT_SECRET_KEY=<your-secret-key>
JWT_EXPIRATION_ACCESS=3600000
JWT_EXPIRATION_REFRESH=604800000
JWT_EXPIRATION_SIGNUP=300000

# GitHub
GITHUB_CLIENT_ID=<your-client-id>
GITHUB_CLIENT_SECRET=<your-client-secret>

# AWS
AWS_ACCESS_KEY=<your-access-key>
AWS_SECRET_KEY=<your-secret-key>
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=kosp-uploads
```

**`infra/db/.env`** (Docker Compose):
```bash
# PostgreSQL
POSTGRES_DB=kosp
POSTGRES_USER=kosp
POSTGRES_PASSWORD=kosp

# RabbitMQ
RABBITMQ_DEFAULT_USER=admin
RABBITMQ_DEFAULT_PASS=admin

# MongoDB
MONGO_INITDB_ROOT_USERNAME=kosp
MONGO_INITDB_ROOT_PASSWORD=kosp
MONGO_INITDB_DATABASE=kosp
```

3. **Start infrastructure**:
```bash
cd infra/db
docker-compose up -d
```

**Verify services**:
```bash
docker ps --filter "name=kosp-compose"
# Should show: postgres, rabbitmq, valkey, mongo
```

4. **Build all services**:
```bash
./gradlew build
```

---

## Development Guide

### Running Services Locally

#### Option 1: Run All Services (Production-like)
```bash
# Terminal 1: api-service
./gradlew :api-service:bootRun

# Terminal 2: challenge-service
./gradlew :challenge-service:bootRun

# Terminal 3: notification-service
./gradlew :notification-service:bootRun

# Terminal 4: harvester (optional, for batch jobs)
./gradlew :harvester:bootRun
```

#### Option 2: Run Individual Service (Development)
```bash
# Only run the service you're working on
./gradlew :api-service:bootRun

# Other services can be mocked or stubbed
```

### Adding a New Event

**Step 1**: Create Event DTO in `common/`
```java
// common/src/main/java/io/swkoreatech/kosp/common/event/MyEvent.java
public record MyEvent(
    String messageId,
    Long userId,
    String data
) {}
```

**Step 2**: Add Queue Name
```java
// infra/rabbitmq/src/main/java/io/swkoreatech/kosp/infra/rabbitmq/constants/QueueNames.java
public static final String MY_QUEUE = "my-queue";
```

**Step 3**: Configure Queue
```java
// infra/rabbitmq/src/main/java/io/swkoreatech/kosp/infra/rabbitmq/config/RabbitMQConfig.java
@Bean
public Queue myQueue() {
    return QueueBuilder.durable(QueueNames.MY_QUEUE)
        .withArgument("x-dead-letter-routing-key", QueueNames.MY_QUEUE + ".dlq")
        .build();
}

@Bean
public Queue myQueueDLQ() {
    return QueueBuilder.durable(QueueNames.MY_QUEUE + ".dlq").build();
}
```

**Step 4**: Publish Event (with Outbox Pattern)
```java
@Transactional
public void publishMyEvent(Long userId, String data) {
    MyEvent event = new MyEvent(UUID.randomUUID().toString(), userId, data);
    
    outboxMessageRepository.save(new OutboxMessage(
        event.messageId(),
        "MyEvent",
        objectMapper.writeValueAsString(event),
        OutboxStatus.PENDING
    ));
}
```

**Step 5**: Create Listener
```java
@RabbitListener(queues = QueueNames.MY_QUEUE, concurrency = "5")
@Transactional
public void handleMyEvent(MyEvent event, Channel channel, 
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
    if (processedMessageRepository.existsByMessageId(event.messageId())) {
        channel.basicAck(tag, false);
        return;
    }
    
    try {
        // Process event
        log.info("Received: {}", event);
        
        processedMessageRepository.save(
            new ProcessedMessage(event.messageId(), "MyEvent")
        );
        
        channel.basicAck(tag, false);
    } catch (Exception e) {
        log.error("Failed", e);
        channel.basicNack(tag, false, false);
    }
}
```

---

## Testing

### Unit Tests
```bash
# Test single service
./gradlew :api-service:test
./gradlew :challenge-service:test
./gradlew :notification-service:test

# Test all
./gradlew test
```

### Integration Tests

Integration tests require full infrastructure (PostgreSQL, RabbitMQ, Redis, MongoDB).

```bash
# Start infrastructure
cd infra/db && docker-compose up -d

# Run integration tests
./gradlew integrationTest

# Or test specific service
./gradlew :api-service:integrationTest
```

### Manual End-to-End Testing

**Test SSE Notifications**:
```bash
# Terminal 1: Subscribe to SSE
curl -N http://localhost:8080/v1/notifications/subscribe/1

# Terminal 2: Trigger challenge evaluation
curl -X POST http://localhost:8080/v1/users/1/challenges/refresh \
  -H "Authorization: Bearer <token>"

# Expected: Terminal 1 should receive SSE message
```

**Test RabbitMQ Management UI**:
```
http://localhost:15672
Username: admin
Password: admin
```

**Verify Queues**:
- challenge-evaluation-queue
- challenge-completed-queue
- point-changed-queue
- (+ 3 DLQs)

---

## Deployment

### Docker Build

Each service has a Dockerfile in its root directory.

```bash
# Build api-service
docker build -t kosp/api-service:latest -f api-service/Dockerfile .

# Build challenge-service
docker build -t kosp/challenge-service:latest -f challenge-service/Dockerfile .

# Build notification-service
docker build -t kosp/notification-service:latest -f notification-service/Dockerfile .

# Build harvester
docker build -t kosp/harvester:latest -f harvester/Dockerfile .
```

### Docker Compose (Full Stack)

**Production docker-compose.yml** (example):
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:18.1
    environment:
      POSTGRES_DB: kosp
      POSTGRES_USER: kosp
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}
    ports:
      - "5672:5672"
      - "15672:15672"

  api-service:
    image: kosp/api-service:latest
    environment:
      DB_HOST: postgres
      RABBITMQ_HOST: rabbitmq
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - rabbitmq

  challenge-service:
    image: kosp/challenge-service:latest
    environment:
      DB_HOST: postgres
      RABBITMQ_HOST: rabbitmq
    depends_on:
      - postgres
      - rabbitmq

  notification-service:
    image: kosp/notification-service:latest
    environment:
      DB_HOST: postgres
      RABBITMQ_HOST: rabbitmq
    ports:
      - "8081:8081"
    depends_on:
      - postgres
      - rabbitmq

volumes:
  postgres-data:
```

---

## Troubleshooting

### Common Issues

#### 1. Service Won't Start

**Symptom**: Service crashes on startup with connection errors.

**Solution**:
```bash
# Check infrastructure services
docker ps --filter "name=kosp-compose"

# Check logs
docker logs postgres-kosp-compose
docker logs rabbitmq-kosp-compose

# Verify environment variables
cat .env.local
```

---

#### 2. RabbitMQ Connection Refused

**Symptom**: `java.net.ConnectException: Connection refused`

**Solution**:
```bash
# Check RabbitMQ is running
docker ps --filter "name=rabbitmq"

# Check RabbitMQ logs
docker logs rabbitmq-kosp-compose

# Verify credentials
docker exec rabbitmq-kosp-compose rabbitmqctl list_users
```

---

#### 3. Messages Stuck in Queue

**Symptom**: Messages remain in queue but are not processed.

**Debug Steps**:
```bash
# 1. Check RabbitMQ Management UI
http://localhost:15672

# 2. Check service logs
docker logs challenge-service-kosp-compose

# 3. Check for exceptions in listener
grep "ERROR" challenge-service.log

# 4. Check DLQ for failed messages
# In RabbitMQ UI: Queues → challenge-evaluation-queue.dlq
```

**Common Causes**:
- Listener not running (service crashed)
- Exception in listener (check DLQ)
- Idempotency check blocking message (check processed_messages table)

---

#### 4. SSE Connection Drops

**Symptom**: Frontend receives `EventSource` error, connection closes.

**Solution**:
```bash
# 1. Check notification-service logs
docker logs notification-service-kosp-compose

# 2. Check Nginx timeout configuration
cat infra/backend/nginx/default.conf.template | grep timeout

# 3. Increase SSE timeout (if needed)
# In NotificationService.java:
private static final Long SSE_TIMEOUT = 60L * 60 * 1000;  // 60 minutes
```

---

#### 5. Database Migration Fails

**Symptom**: Flyway migration error on startup.

**Solution**:
```bash
# 1. Check current migration version
docker exec postgres-kosp-compose psql -U kosp -d kosp -c "SELECT * FROM flyway_schema_history;"

# 2. Repair Flyway (if migration failed midway)
./gradlew flywayRepair

# 3. Re-run application
./gradlew :api-service:bootRun
```

---

### Monitoring Commands

```bash
# Check RabbitMQ queue status
docker exec rabbitmq-kosp-compose rabbitmqctl list_queues name messages consumers

# Check PostgreSQL connections
docker exec postgres-kosp-compose psql -U kosp -d kosp -c "SELECT count(*) FROM pg_stat_activity;"

# Check outbox messages
docker exec postgres-kosp-compose psql -U kosp -d kosp -c "SELECT status, count(*) FROM outbox_messages GROUP BY status;"

# Check processed messages
docker exec postgres-kosp-compose psql -U kosp -d kosp -c "SELECT count(*) FROM processed_messages;"
```

---

## Appendix

### RabbitMQ Queue Reference

| Queue Name | Publisher | Consumer | Event Type | Concurrency |
|------------|-----------|----------|------------|-------------|
| `challenge-evaluation-queue` | harvester | challenge-service | ChallengeEvaluationRequest | 5 |
| `challenge-completed-queue` | challenge-service | notification-service | ChallengeCompletedEvent | 5 |
| `point-changed-queue` | challenge-service | notification-service | PointChangedEvent | 5 |

### Database Tables Reference

| Table | Purpose | Module |
|-------|---------|--------|
| `outbox_messages` | Transactional event publishing | All services |
| `processed_messages` | Idempotency tracking | challenge-service, notification-service |
| `users` | User identity | api-service, challenge-service |
| `challenges` | Challenge definitions | api-service, challenge-service |

### Service Ports

| Service | Port | Protocol | Purpose |
|---------|------|----------|---------|
| api-service | 8080 | HTTP | REST API |
| notification-service | 8081 | HTTP | SSE endpoint |
| challenge-service | - | - | No HTTP (worker) |
| harvester | - | - | No HTTP (batch) |
| RabbitMQ | 5672 | AMQP | Message broker |
| RabbitMQ | 15672 | HTTP | Management UI |
| PostgreSQL | 5432 | TCP | Database |

---

## Migration History

| Date | Version | Changes |
|------|---------|---------|
| 2026-01-31 | 1.0.0 | Initial MSA migration complete |

---

**Questions?** Contact: KOSP Backend Team
