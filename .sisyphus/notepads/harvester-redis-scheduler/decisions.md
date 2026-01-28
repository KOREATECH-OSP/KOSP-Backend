## [2026-01-27] Final Architecture Decisions

### Redis Queue Design
**Decision**: Use Redis Sorted Set with priority-based scoring
**Rationale**: 
- Provides persistence across app restarts
- Native support for priority via score
- Atomic operations (ZADD, ZPOPMIN)
- Simple and efficient

**Implementation**:
- Key: `job:queue`
- Score: `priority_offset + scheduledAt.getEpochSecond()`
- Member: `"userId:runId"`
- HIGH_PRIORITY_OFFSET = 0L
- LOW_PRIORITY_OFFSET = 1,000,000,000L

### Spring Batch JobInstance Strategy
**Decision**: Use runId as identifying parameter
**Rationale**:
- Same runId = same JobInstance = auto-resume from failed step
- New runId = new JobInstance = start from beginning
- Enables retry logic without manual step tracking

**Implementation**:
- Success: Generate new UUID → new JobInstance
- Failure: Reuse same UUID → resume JobInstance

### Job Scheduling Logic
**Decision**: Different retry strategies based on failure type
**Rationale**:
- Rate limit errors are temporary and predictable
- Other errors may need more time to resolve
- Success requires regular polling

**Implementation**:
- Success: resetTime + 5min, LOW priority, new UUID
- Rate Limit: resetTime + 5min, HIGH priority, same UUID
- Other errors: now + 30min, HIGH priority, same UUID

### Deleted User Handling
**Decision**: Check before job execution, not in queue
**Rationale**:
- User deletion is rare
- Checking at enqueue time adds complexity
- Checking at execution time is simpler and sufficient

**Implementation**:
- RedisJobQueueListener checks user.isDeleted() before launching job
- If deleted, log and skip (no reschedule)

### Module Organization
**Decision**: JobQueueService in common module
**Rationale**:
- Both backend and harvester need to enqueue jobs
- Shared code reduces duplication
- Common module is already a dependency

**Implementation**:
- common/queue/JobQueueService.java
- common/queue/JobQueueEntry.java (record)
- common/queue/Priority.java (enum)


## [2026-01-27] Comparison with refactoring-issues.md

### What We Actually Did
**Harvester Redis Scheduler Refactoring**:
- Removed MySQL `collection_trigger` table
- Implemented Redis Sorted Set-based job queue
- Added smart retry logic (Rate Limit vs other errors)
- Added duplicate job execution prevention
- Simplified PriorityJobLauncher

**Related to refactoring-issues.md**:
- This work addresses **Issue #7: BE-GH 통신 구조 문제**
  - Specifically: "stream이 안비워짐", "trigger에 쌓여도 실행 안됨"
  - We REMOVED Redis Stream communication entirely
  - Replaced with Redis Sorted Set queue

### Priority Comparison

| 문서 우선순위 | 실제 작업 |
|------------|---------|
| **1. #4 엔티티 중복** | ❌ Not done |
| **2. #7 BE-GH 통신** | ✅ **PARTIALLY DONE** |
| 3. #1, #6 코드 품질 | ⚠️ Improved for queue components |
| 4. #8, #9 챌린지 요청 | ❌ Not done |
| 5. #5 대형 레포 타임아웃 | ❌ Not done |
| 6. #2, #3 데이터 수집 누락 | ❌ Not done |

### Issue #7 Coverage

**From refactoring-issues.md**:
```
## 7. BE-GH 통신 구조 문제
- stream이 안비워짐
- trigger에 쌓여도 실행 안됨 (이게 메시지 유실인가?)
- 메시지 유실 케이스 확인
```

**What we solved**:
- ✅ Removed Redis Stream entirely (no more stream buildup)
- ✅ Replaced with Redis Sorted Set (reliable queue)
- ✅ No more message loss (persistent queue)
- ✅ Triggers now execute reliably (polling + dequeue)

**What remains**:
- Backend challenge check communication (Issue #8, #9)
- This is separate from the trigger mechanism

### Conclusion
We solved **Issue #7 (priority 2)** but NOT **Issue #4 (priority 1)**.

The work was specifically focused on the **trigger/scheduling mechanism**, 
not on entity duplication or shared library creation.

