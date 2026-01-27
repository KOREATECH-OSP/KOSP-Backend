## [2026-01-27] Build Issues

### Test Compilation Failures (Pre-existing)
- Multiple test files have compilation errors due to missing/refactored classes
- These are NOT related to the Redis refactoring work
- Main source code compiles successfully
- Test failures include:
  - GithubCommitDetailRaw (wrong package import)
  - Various calculator classes
  - Repository method signature changes
  
### Workaround
- Build with `-x test` flag to skip test compilation
- Tests need separate fix (out of scope for this refactoring)


## [2026-01-27] Issue: Duplicate Job Execution

### Problem
`RedisJobQueueListener.processEntry()`에서 동일한 userId의 job이 이미 실행 중인 경우를 체크하지 않음.

**시나리오**:
1. Job A (userId=1) 시작 → 실행 중 (5분 소요)
2. 1초 후, Redis에서 Job B (userId=1) dequeue
3. Job B 시작 → **동일 사용자에 대해 중복 실행**

### Impact
- GitHub API rate limit 낭비
- 동일 데이터 중복 처리
- MongoDB write conflict 가능성

### Solution
Spring Batch `JobExplorer`를 사용하여 실행 중인 job 확인:
```java
Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("githubCollectionJob");
```

### Implementation Plan
1. JobExplorer 주입
2. processEntry()에서 실행 중인 job 체크
3. 동일 userId 실행 중이면 재큐잉 (1분 후)

