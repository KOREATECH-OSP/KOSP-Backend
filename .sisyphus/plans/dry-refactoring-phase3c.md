# DRY Refactoring - Phase 3C (PaginationHelper)

## Context

### Original Request
Phase 3에서 복잡도를 이유로 연기했던 PaginationHelper 유틸리티 생성 및 적용

### Phase 3 Results
- 4개 유틸리티 생성 완료
- 7개 Step 파일 리팩토링 완료
- 144 LOC 감소 달성
- Phase 3는 production stable 상태

### Phase 3C Scope
페이지네이션 로직을 PaginationHelper 유틸리티로 추출하여 추가 35 LOC 감소

---

## Work Objectives

### Core Objective
3개 Step 파일의 서로 다른 페이지네이션 패턴을 단일 유틸리티로 통합

### Concrete Deliverables
1. **PaginationHelper 유틸리티** (1개 클래스)
   - Generic pagination handler with callback pattern
   
2. **3개 Step 파일 리팩토링**:
   - CommitMiningStep.java (recursive pagination)
   - PullRequestMiningStep.java (do-while pagination)
   - IssueMiningStep.java (do-while pagination)

3. **테스트**:
   - PaginationHelperTest.java (unit tests)
   - 기존 Step 통합 테스트 통과 확인

### Definition of Done
- [ ] PaginationHelper 유틸리티 생성 완료
- [ ] 3개 Step 파일 페이지네이션 로직 교체 완료
- [ ] 단위 테스트 작성 완료 (80%+ coverage)
- [ ] 전체 빌드 성공
- [ ] LOC 감소: 최소 30줄 이상 (목표: 35줄)
- [ ] 기존 페이지네이션 동작 유지 (회귀 없음)

### Must Have
- Generic callback pattern for different response types
- Support for both recursive and iterative pagination
- Null-safe cursor handling
- PageInfo extraction abstraction

### Must NOT Have (Guardrails)
- No breaking changes to Step file public APIs
- No changes to GraphQL query logic
- No modification to response parsing
- Methods ≤10 lines (KOSP compliance)

---

## Verification Strategy

### After PaginationHelper Creation
```bash
./gradlew :harvester:compileJava -x test
./gradlew :harvester:test --tests "*PaginationHelperTest"
```

### After Each Step Refactoring
```bash
./gradlew :harvester:compileJava -x test
wc -l harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/[StepName].java
```

### Final Verification
```bash
./gradlew :harvester:test
git diff --stat refactor/dry-phase3..HEAD
```

---

## TODOs

---

- [ ] 0. Setup: Create Phase 3C branch from Phase 3

  **What to do**:
  - Create branch: `git checkout -b refactor/dry-phase3c`
  - Verify baseline build

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] Branch created
  - [ ] Build succeeds

  **Commit**: NO

---

- [ ] 1. Create PaginationHelper utility

  **What to do**:
  Create `PaginationHelper.java` with generic pagination handler:
  
  ```java
  public static <T> int paginate(
      String cursor,
      Function<String, GraphQLResponse<T>> fetcher,
      Function<GraphQLResponse<T>, PageInfo> pageInfoExtractor,
      BiConsumer<GraphQLResponse<T>, String> errorHandler,
      Consumer<T> dataProcessor
  )
  ```

  **Pattern Analysis**:
  
  **CommitMiningStep** (recursive):
  ```java
  private int paginateCommits(..., String cursor, int saved) {
      FetchResult result = processCommitsPage(..., cursor, ...);
      saved += result.saved;
      if (!result.hasNextPage) return saved;
      return paginateCommits(..., result.nextCursor, saved);
  }
  ```

  **PullRequestMiningStep** (do-while):
  ```java
  String cursor = null;
  int total = 0;
  do {
      response = fetch(cursor);
      total += process(response);
      pageInfo = extractPageInfo(response);
      cursor = pageInfo.getEndCursor();
  } while (pageInfo.hasNextPage());
  ```

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] File created: `PaginationHelper.java`
  - [ ] Generic pagination method implemented
  - [ ] Supports both recursive and iterative patterns
  - [ ] Private constructor added
  - [ ] Compilation succeeds
  - [ ] Javadoc added

  **Commit**: YES
  - Message: `feat(harvester): add PaginationHelper utility`

---

- [ ] 2. Refactor CommitMiningStep pagination

  **What to do**:
  - Replace `paginateCommits()` recursive method with PaginationHelper
  - Replace `FetchResult` inner class usage
  - Update method signatures

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] Old pagination method removed
  - [ ] Uses PaginationHelper
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~12 lines

  **Commit**: YES
  - Message: `refactor(harvester): use PaginationHelper in CommitMiningStep`

---

- [ ] 3. Refactor PullRequestMiningStep pagination

  **What to do**:
  - Replace `fetchAllPullRequests()` do-while loop with PaginationHelper
  - Remove manual cursor management

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] Old pagination loop removed
  - [ ] Uses PaginationHelper
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~10 lines

  **Commit**: YES
  - Message: `refactor(harvester): use PaginationHelper in PullRequestMiningStep`

---

- [ ] 4. Refactor IssueMiningStep pagination

  **What to do**:
  - Replace `fetchAllIssues()` do-while loop with PaginationHelper
  - Remove manual cursor management

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] Old pagination loop removed
  - [ ] Uses PaginationHelper
  - [ ] Compilation succeeds
  - [ ] LOC reduced by ~10 lines

  **Commit**: YES
  - Message: `refactor(harvester): use PaginationHelper in IssueMiningStep`

---

- [ ] 5. Add unit tests for PaginationHelper

  **What to do**:
  Create `PaginationHelperTest.java`:
  - Test single page response
  - Test multi-page pagination
  - Test error handling
  - Test null cursor handling

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] Test file created
  - [ ] All tests pass
  - [ ] Coverage ≥80%

  **Commit**: YES
  - Message: `test(harvester): add PaginationHelperTest`

---

- [ ] 6. Final Verification and Documentation

  **What to do**:
  - Run full build (all modules)
  - Run all harvester tests
  - Count LOC reduction
  - Update AGENTS.md with PaginationHelper example
  - Update completion summary

  **Parallelizable**: NO

  **Acceptance Criteria**:
  - [ ] All builds succeed
  - [ ] All tests pass
  - [ ] LOC reduced by ≥30 lines
  - [ ] Documentation updated

  **Commit**: YES
  - Message: `docs: complete Phase 3C DRY refactoring`

---

## Success Criteria

### Code Quality
- [ ] LOC reduction: ≥30 lines (target: 35 lines)
- [ ] PaginationHelper follows KOSP rules
- [ ] Test coverage ≥80%

### Functionality
- [ ] All builds succeed
- [ ] All tests pass
- [ ] No behavioral changes (pagination works identically)

### Documentation
- [ ] AGENTS.md updated with PaginationHelper example
- [ ] Completion summary created
