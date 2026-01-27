# DRY Refactoring - Phase 3 (Code Deduplication)

## Context

### Original Request
Harvester 코드베이스에서 중복 코드를 유틸리티로 추출하여 DRY 원칙 적용

### Research Findings

**코드 중복 분석 결과**:
- 총 7개 Step 파일: 1,498 LOC
- 발견된 중복 패턴: 7가지
- 추출 가능한 LOC: 184줄 (12.3% 감소 가능)
- 예상 결과: 1,314 LOC

**중복 패턴 Top 5**:
1. ExecutionContext 추출 (6파일, 42줄)
2. GraphQL 에러 처리 (4파일, 28줄)
3. GraphQL 타입 캐스팅 (4파일, 16줄)
4. 페이지네이션 로직 (3파일, 35줄)
5. Null-Safe Getter (2파일, 24줄)

---

## Work Objectives

### Core Objective
중복 코드를 유틸리티 클래스로 추출하여 유지보수성 향상 및 코드 품질 개선

### Concrete Deliverables
1. **5개 유틸리티 클래스 생성**:
   - StepContextHelper.java
   - NullSafeGetters.java
   - GraphQLErrorHandler.java
   - GraphQLTypeFactory.java
   - PaginationHelper.java

2. **7개 Step 파일 리팩토링**:
   - CommitMiningStep.java
   - RepositoryDiscoveryStep.java
   - PullRequestMiningStep.java
   - IssueMiningStep.java
   - CleanupStep.java
   - ScoreCalculationStep.java
   - StatisticsAggregationStep.java

3. **유틸리티 단위 테스트**: 5개 테스트 클래스

### Definition of Done
- [ ] 5개 유틸리티 클래스 생성 완료
- [ ] 7개 Step 파일에서 중복 코드 제거 완료
- [ ] 유틸리티 테스트 작성 완료
- [ ] 전체 빌드 성공 (common, harvester, backend)
- [ ] LOC 감소: 최소 150줄 이상 (목표: 184줄)
- [ ] 기존 기능 동작 유지 (회귀 없음)

### Must Have
- 유틸리티는 `harvester/collection/util/` 패키지에 위치
- Private 생성자 (유틸리티 클래스 인스턴스화 방지)
- Javadoc 주석 (각 public 메서드)
- Null-safety 보장 (@NonNull, @Nullable 어노테이션)

### Must NOT Have (Guardrails)
- **No business logic in utilities**: 순수 헬퍼 메서드만
- **No state**: 모든 메서드는 static, stateless
- **No KOSP rule violations**: 메서드 ≤10줄 유지
- **No breaking changes**: 기존 Step 파일의 public API 변경 금지

---

## Verification Strategy

### After Each Utility Creation
```bash
# Compilation
./gradlew :harvester:compileJava -x test

# Test execution
./gradlew :harvester:test --tests "*[UtilityName]Test"
```

### After Each Step Refactoring
```bash
# Compilation
./gradlew :harvester:compileJava -x test

# LOC count
wc -l harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/[StepName].java
```

### Final Verification
```bash
# 1. Full build
./gradlew :common:compileJava :harvester:compileJava :backend:compileJava -x test

# 2. All tests pass
./gradlew :harvester:test

# 3. LOC reduction check
git diff --stat refactor/kosp-compliance-phase2..HEAD
```

---

## Task Flow

```
Task 0 (Setup)
  ↓
Task 1-5 (Create Utilities) ← Can be parallelized
  ↓
Task 6-12 (Refactor Step Files) ← Sequential (one by one)
  ↓
Task 13 (Add Tests)
  ↓
Task 14 (Final Verification)
```

---

## TODOs

---

- [ ] 0. Setup: Create feature branch and prepare utility package

  **What to do**:
  - Create feature branch: `git checkout -b refactor/dry-phase3`
  - Create utility package directory: `harvester/src/main/java/io/swkoreatech/kosp/collection/util/`
  - Create test directory: `harvester/src/test/java/io/swkoreatech/kosp/collection/util/`
  - Verify baseline build

  **Parallelizable**: NO (foundation)

  **Acceptance Criteria**:
  - [ ] Branch created: `git branch --show-current` → `refactor/dry-phase3`
  - [ ] Directories created and exist
  - [ ] Build succeeds: `./gradlew :harvester:compileJava -x test` → SUCCESS

  **Commit**: NO (no code changes yet)

---

- [ ] 1. Create StepContextHelper utility

  **What to do**:
  Create `StepContextHelper.java` with:
  - `getExecutionContext(ChunkContext)` → ExecutionContext
  - `extractUserId(ChunkContext)` → Long
  - `extractString(ChunkContext, String key)` → String
  - `putString(ChunkContext, String key, String value)` → void

  **Pattern to extract**:
  ```java
  // FROM (6 occurrences):
  private ExecutionContext getExecutionContext(ChunkContext ctx) {
      return ctx.getStepContext()
          .getStepExecution()
          .getJobExecution()
          .getExecutionContext();
  }
  
  // TO:
  ExecutionContext context = StepContextHelper.getExecutionContext(chunkContext);
  ```

  **Parallelizable**: YES (with Task 2-5)

  **References**:
  - CommitMiningStep.java:77-82, 84-89
  - PullRequestMiningStep.java:71-76, 78-83
  - IssueMiningStep.java:71-76, 78-83
  - RepositoryDiscoveryStep.java:64-69
  - ScoreCalculationStep.java:69-74
  - StatisticsAggregationStep.java:71-76
  - CleanupStep.java:55-60

  **Acceptance Criteria**:
  - [ ] File created: `harvester/collection/util/StepContextHelper.java`
  - [ ] All 4 methods implemented
  - [ ] Private constructor added
  - [ ] Compilation succeeds
  - [ ] Javadoc added for all public methods

  **Commit**: YES
  - Message: `feat(harvester): add StepContextHelper utility`
  - Files: `StepContextHelper.java`

---

- [ ] 2. Create NullSafeGetters utility

  **What to do**:
  Create `NullSafeGetters.java` with:
  - `intOrZero(Integer value)` → int
  - `longOrZero(Long value)` → long
  - `intOrZero(Supplier<Integer> supplier)` → int
  
  **Pattern to extract**:
  ```java
  // FROM (6 occurrences):
  private int getAdditionsOrZero(CommitDocument commit) {
      if (commit.getAdditions() == null) {
          return 0;
      }
      return commit.getAdditions();
  }
  
  // TO:
  int additions = NullSafeGetters.intOrZero(commit.getAdditions());
  ```

  **Parallelizable**: YES (with Task 1, 3-5)

  **References**:
  - ScoreCalculationStep.java:219-224 (getClosedIssuesCountOrZero)
  - StatisticsAggregationStep.java:203-229 (6 methods: getAdditionsOrZero, getDeletionsOrZero, getStargazersCountOrZero, getForksCountOrZero)

  **Acceptance Criteria**:
  - [ ] File created: `harvester/collection/util/NullSafeGetters.java`
  - [ ] All 3 methods implemented
  - [ ] Private constructor added
  - [ ] Compilation succeeds

  **Commit**: YES
  - Message: `feat(harvester): add NullSafeGetters utility`

---

- [ ] 3. Create GraphQLErrorHandler utility

  **What to do**:
  Create `GraphQLErrorHandler.java` with:
  - `logAndCheckErrors(GraphQLResponse<?> response, String entityType, String entityId)` → boolean (returns true if has errors)
  - `handleErrors(GraphQLResponse<?> response, String entityType, String entityId)` → void (throws if errors)

  **Pattern to extract**:
  ```java
  // FROM (4 occurrences):
  private void logErrors(GraphQLResponse<...> response, String login) {
      if (response == null) {
          log.warn("No response from GraphQL for user {}", login);
          return;
      }
      log.error("GraphQL errors for user {}: {}", login, response.getErrors());
  }
  
  // TO:
  if (GraphQLErrorHandler.logAndCheckErrors(response, "user", login)) {
      return; // has errors
  }
  ```

  **Parallelizable**: YES (with Task 1-2, 4-5)

  **References**:
  - CommitMiningStep.java:190-196 (logErrors)
  - PullRequestMiningStep.java:120-126 (logErrors)
  - IssueMiningStep.java:120-126 (logErrors)
  - RepositoryDiscoveryStep.java:122-128 (logErrors)

  **Acceptance Criteria**:
  - [ ] File created: `harvester/collection/util/GraphQLErrorHandler.java`
  - [ ] Both methods implemented
  - [ ] Uses Lombok @Slf4j for logging
  - [ ] Compilation succeeds

  **Commit**: YES
  - Message: `feat(harvester): add GraphQLErrorHandler utility`

---

- [ ] 4. Create GraphQLTypeFactory utility

  **What to do**:
  Create `GraphQLTypeFactory.java` with:
  - `<T> responseType()` → Class<GraphQLResponse<T>>

  **Pattern to extract**:
  ```java
  // FROM (4 occurrences):
  @SuppressWarnings("unchecked")
  private Class<GraphQLResponse<UserPullRequestsResponse>> createResponseType() {
      return (Class<GraphQLResponse<UserPullRequestsResponse>>) (Class<?>) GraphQLResponse.class;
  }
  
  // TO:
  Class<GraphQLResponse<UserPullRequestsResponse>> responseType = GraphQLTypeFactory.responseType();
  ```

  **Parallelizable**: YES (with Task 1-3, 5)

  **References**:
  - CommitMiningStep.java:185-188 (createResponseType)
  - PullRequestMiningStep.java:115-118 (createResponseType)
  - IssueMiningStep.java:115-118 (createResponseType)
  - RepositoryDiscoveryStep.java:134-138 (createResponseType)

  **Acceptance Criteria**:
  - [ ] File created: `harvester/collection/util/GraphQLTypeFactory.java`
  - [ ] Generic method implemented
  - [ ] @SuppressWarnings moved to utility
  - [ ] Compilation succeeds

  **Commit**: YES
  - Message: `feat(harvester): add GraphQLTypeFactory utility`

---

- [ ] 5. Create PaginationHelper utility (Most Complex)

  **What to do**:
  Create `PaginationHelper.java` with generic pagination support:
  - `<T, R> paginate(FetchFunction<T> fetcher, PageInfoExtractor<T> pageInfoExtractor, ItemProcessor<T> processor)`
  - Support for both recursive and iterative pagination
  - Built-in error handling integration with GraphQLErrorHandler

  **Pattern to extract**:
  ```java
  // FROM: 3 different implementations
  // CommitMiningStep: Recursive with FetchResult
  // PullRequestMiningStep: Do-while loop
  // IssueMiningStep: Do-while loop
  
  // TO:
  PaginationHelper.paginate(
      cursor -> graphQLClient.fetch(cursor),
      response -> response.getPageInfo(),
      items -> processAndSave(items)
  );
  ```

  **Parallelizable**: YES (with Task 1-4)

  **References**:
  - CommitMiningStep.java:119-139 (fetchAllCommits + paginateCommits)
  - PullRequestMiningStep.java:85-109 (fetchAllPullRequests)
  - IssueMiningStep.java:85-109 (fetchAllIssues)

  **Acceptance Criteria**:
  - [ ] File created: `harvester/collection/util/PaginationHelper.java`
  - [ ] Functional interfaces defined (FetchFunction, PageInfoExtractor, ItemProcessor)
  - [ ] Pagination method implemented
  - [ ] Error handling integrated
  - [ ] Compilation succeeds

  **Commit**: YES
  - Message: `feat(harvester): add PaginationHelper utility for GraphQL pagination`

---

- [ ] 6. Refactor CommitMiningStep (uses all 5 utilities)

  **What to do**:
  - Replace getExecutionContext() with StepContextHelper
  - Replace extractUserId() with StepContextHelper
  - Replace logErrors() with GraphQLErrorHandler
  - Replace createResponseType() with GraphQLTypeFactory
  - Replace paginateCommits() with PaginationHelper

  **Parallelizable**: NO (must verify each step individually)

  **References**:
  - Task 1-5 utilities

  **Acceptance Criteria**:
  - [ ] All utility imports added
  - [ ] Old helper methods removed
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~30+ lines

  **Commit**: YES
  - Message: `refactor(harvester): use utilities in CommitMiningStep`

---

- [ ] 7. Refactor PullRequestMiningStep

  **What to do**:
  - Replace getExecutionContext() with StepContextHelper
  - Replace extractUserId() with StepContextHelper
  - Replace logErrors() with GraphQLErrorHandler
  - Replace createResponseType() with GraphQLTypeFactory
  - Replace fetchAllPullRequests() with PaginationHelper

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] All utility imports added
  - [ ] Old helper methods removed
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~25+ lines

  **Commit**: YES
  - Message: `refactor(harvester): use utilities in PullRequestMiningStep`

---

- [ ] 8. Refactor IssueMiningStep

  **What to do**:
  - Replace getExecutionContext() with StepContextHelper
  - Replace extractUserId() with StepContextHelper
  - Replace logErrors() with GraphQLErrorHandler
  - Replace createResponseType() with GraphQLTypeFactory
  - Replace fetchAllIssues() with PaginationHelper

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] All utility imports added
  - [ ] Old helper methods removed
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~25+ lines

  **Commit**: YES
  - Message: `refactor(harvester): use utilities in IssueMiningStep`

---

- [ ] 9. Refactor RepositoryDiscoveryStep

  **What to do**:
  - Replace extractUserId() with StepContextHelper
  - Replace logErrors() with GraphQLErrorHandler
  - Replace createResponseType() with GraphQLTypeFactory

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] All utility imports added
  - [ ] Old helper methods removed
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~15+ lines

  **Commit**: YES
  - Message: `refactor(harvester): use utilities in RepositoryDiscoveryStep`

---

- [ ] 10. Refactor ScoreCalculationStep

  **What to do**:
  - Replace extractUserId() with StepContextHelper
  - Replace getClosedIssuesCountOrZero() with NullSafeGetters

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] All utility imports added
  - [ ] Old helper methods removed
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~10+ lines

  **Commit**: YES
  - Message: `refactor(harvester): use utilities in ScoreCalculationStep`

---

- [ ] 11. Refactor StatisticsAggregationStep

  **What to do**:
  - Replace extractUserId() with StepContextHelper
  - Replace 6 null-safe getter methods with NullSafeGetters

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] All utility imports added
  - [ ] Old helper methods removed (6 methods)
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~30+ lines

  **Commit**: YES
  - Message: `refactor(harvester): use utilities in StatisticsAggregationStep`

---

- [ ] 12. Refactor CleanupStep

  **What to do**:
  - Replace extractUserId() with StepContextHelper

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] Utility import added
  - [ ] Old helper method removed
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~5+ lines

  **Commit**: YES
  - Message: `refactor(harvester): use utilities in CleanupStep`

---

- [ ] 13. Add unit tests for utilities

  **What to do**:
  Create test classes:
  - StepContextHelperTest.java
  - NullSafeGettersTest.java
  - GraphQLErrorHandlerTest.java
  - GraphQLTypeFactoryTest.java
  - PaginationHelperTest.java

  **Test coverage target**: 80%+ for each utility

  **Parallelizable**: NO (depends on Tasks 1-5)

  **Acceptance Criteria**:
  - [ ] 5 test classes created
  - [ ] All tests pass: `./gradlew :harvester:test --tests "*util*"`
  - [ ] Coverage ≥80% for util package

  **Commit**: YES
  - Message: `test(harvester): add unit tests for utility classes`

---

- [ ] 14. Final Verification and Documentation

  **What to do**:
  
  **Verification**:
  - Run full build (all modules)
  - Count LOC reduction
  - Verify no regressions
  - Check test coverage
  
  **Documentation**:
  - Update `harvester/AGENTS.md` with utility usage examples
  - Update `docs/todo/refactoring-issues.md` to mark Phase 3 complete
  - Create completion summary in `.sisyphus/notepads/dry-phase3/`

  **Parallelizable**: NO

  **Acceptance Criteria**:
  
  **Build Verification**:
  - [ ] `./gradlew :common:compileJava -x test` → SUCCESS
  - [ ] `./gradlew :harvester:compileJava -x test` → SUCCESS
  - [ ] `./gradlew :backend:compileJava -x test` → SUCCESS
  
  **Test Verification**:
  - [ ] `./gradlew :harvester:test` → ALL PASS
  
  **LOC Reduction**:
  - [ ] Total reduction ≥150 lines (goal: 184 lines)
  - [ ] Step files: from 1,498 → ~1,314 LOC
  
  **Documentation**:
  - [ ] `harvester/AGENTS.md` updated with utility examples
  - [ ] `docs/todo/refactoring-issues.md` Phase 3 marked complete
  - [ ] Completion summary created

  **Commit**: YES
  - Message: `docs: mark Phase 3 DRY refactoring complete`

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(harvester): add StepContextHelper utility` | StepContextHelper.java | compilation |
| 2 | `feat(harvester): add NullSafeGetters utility` | NullSafeGetters.java | compilation |
| 3 | `feat(harvester): add GraphQLErrorHandler utility` | GraphQLErrorHandler.java | compilation |
| 4 | `feat(harvester): add GraphQLTypeFactory utility` | GraphQLTypeFactory.java | compilation |
| 5 | `feat(harvester): add PaginationHelper utility` | PaginationHelper.java | compilation |
| 6 | `refactor(harvester): use utilities in CommitMiningStep` | CommitMiningStep.java | compilation + LOC count |
| 7-12 | Similar refactor messages for each Step | 6 Step files | compilation + LOC count |
| 13 | `test(harvester): add unit tests for utility classes` | 5 test files | test execution |
| 14 | `docs: mark Phase 3 DRY refactoring complete` | 3 docs | full build + tests |

---

## Success Criteria

### Code Quality
- [ ] LOC reduction: ≥150 lines (target: 184 lines)
- [ ] No code duplication in Step files
- [ ] All utilities follow KOSP rules (methods ≤10 lines)
- [ ] Test coverage ≥80% for util package

### Functionality
- [ ] All builds succeed (common, harvester, backend)
- [ ] All tests pass
- [ ] No behavioral changes (regression-free)

### Documentation
- [ ] Utility usage documented in AGENTS.md
- [ ] Phase 3 marked complete in refactoring-issues.md
- [ ] Completion summary created
