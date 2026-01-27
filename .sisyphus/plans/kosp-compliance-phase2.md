# Code Quality Refactoring - Phase 2 (Harvester 전체 + Priority enum)

## Context

### Original Request
1. Harvester 모든 Step 파일에 KOSP 규칙 적용 (Phase 1에서 2개만 완료, 나머지 5개 필요)
2. Priority enum의 offset을 static final이 아닌 enum 필드로 관리

### Research Findings

**Phase 1 완료 현황**:
- ✅ ScoreCalculationStep.java: 삼항 연산자 5개 제거, 긴 메서드 4개 분할
- ✅ StatisticsAggregationStep.java: 삼항 연산자 4개 제거, 긴 메서드 3개 분할
- 브랜치: `refactor/kosp-compliance-phase1` (4개 커밋)

**Phase 2 대상 분석**:
- 나머지 5개 Step 파일 중 위반 발견: 6개 메서드 (11-25줄 범위)
- 삼항 연산자: 0개 (이미 정리됨)
- CleanupStep.java: 이미 KOSP 완전 준수 (작업 불필요, 검증만)

**Priority enum 문제**:
- 중복 존재: `common/queue/Priority` vs `harvester/launcher/Priority`
- `harvester/launcher/Priority`의 `order` 필드: 사용 안 됨 (dead code)
- `JobQueueService`에 static final로 offset 관리 (enum과 분리됨)

---

## Work Objectives

### Core Objective
1. Priority enum을 하나로 통합하고 offset을 enum 필드로 관리
2. 나머지 5개 Harvester Step 파일을 KOSP 규칙 준수하도록 리팩토링

### Concrete Deliverables
1. **Priority enum 간소화**:
   - `common/queue/Priority.java`: offset 필드 추가
   - `common/queue/JobQueueService.java`: static final 제거
   - `harvester/launcher/Priority.java`: 파일 삭제 (dead code)

2. **Harvester Step 리팩토링**:
   - CommitMiningStep.java: 2개 메서드 분할
   - RepositoryDiscoveryStep.java: 2개 메서드 분할
   - PullRequestMiningStep.java: 1개 메서드 분할
   - IssueMiningStep.java: 1개 메서드 분할
   - CleanupStep.java: 검증만 (이미 준수)

### Definition of Done
- [x] Priority enum 중복 제거 (common 1개만)
- [x] harvester/launcher/Priority.java 삭제
- [x] JobQueueService에서 static final 제거, enum getOffset() 사용
- [x] 6개 긴 메서드 분할 완료 (모든 메서드 ≤10줄)
- [x] Backend + Harvester + Common 빌드 성공
- [x] AST grep: 0 ternaries (harvester 전체)

### Must Have
- Early return pattern 유지
- Private helper methods for extractions
- Atomic commits per task
- Compilation verification after EACH task

### Must NOT Have (Guardrails)
- **No utility extraction**: Phase 2는 KOSP 준수만, 유틸리티는 Phase 3
- **No test modifications**: 테스트 파일 건드리지 않음
- **No algorithm changes**: 비즈니스 로직 변경 금지
- **No framework entry point modification**: execute() 메서드는 예외 (10줄 초과 허용)

---

## Verification Strategy

### After Each Task
```bash
# Compilation
./gradlew :common:compileJava -x test       # Task 1 후
./gradlew :harvester:compileJava -x test    # Task 2-6 후
./gradlew :backend:compileJava -x test      # Task 1 후 (Priority 사용처)

# File isolation
git diff --name-only  # 해당 task 파일만

# Line count (manual)
# 모든 메서드 ≤10줄 검증 (execute() 제외)
```

### Final Verification (Task 7)
```bash
# 1. Ternary check
ast_grep_search(
  pattern="$VAR ? $A : $B",
  lang="java",
  paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl"]
)
# Expected: 0 matches

# 2. Build check (all modules)
./gradlew :common:compileJava -x test
./gradlew :harvester:compileJava -x test  
./gradlew :backend:compileJava -x test
# Expected: ALL BUILD SUCCESSFUL

# 3. File diff
git diff --name-only origin/refactor/kosp-compliance-phase1
# Expected: 9 files (3 common + 4 harvester + 2 docs)

# 4. Manual line count audit
# All methods ≤10 lines (execute() 제외)
```

---

## Task Flow

```
Task 0 → Task 1 (Priority) → Task 2-6 (Steps) → Task 7 (Verification)
```

**No parallelization**: Sequential execution. Task 1 must complete before Task 2-6.

---

## TODOs

---

- [ ] 0. Setup: Create feature branch and verify baseline

  **What to do**:
  - Create feature branch: `git checkout -b refactor/kosp-compliance-phase2`
  - Verify current build state: `./gradlew :harvester:compileJava -x test` → expect SUCCESS
  - Verify current branch: `git branch --show-current`

  **Parallelizable**: NO (foundation for all tasks)

  **References**:
  - `AGENTS.md` - KOSP Backend coding rules
  - `harvester/AGENTS.md` - Harvester module specifics
  - `.sisyphus/plans/code-quality-refactoring.md` - Phase 1 plan

  **Acceptance Criteria**:
  - [ ] Branch created: `git branch --show-current` → `refactor/kosp-compliance-phase2`
  - [ ] Build succeeds: `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL

  **Commit**: NO (no changes yet)

---

- [ ] 1. Priority enum 간소화

  **What to do**:
  
  **Step 1.1**: Modify `common/queue/Priority.java`
  - Add `offset` field with values: HIGH(0L), LOW(1_000_000_000L)
  - Add `getOffset()` method
  
  **Step 1.2**: Modify `common/queue/JobQueueService.java`
  - Remove 2 static final constants: `HIGH_PRIORITY_OFFSET`, `LOW_PRIORITY_OFFSET`
  - Update `calculateScore()` method to use `priority.getOffset()`
  
  **Step 1.3**: Delete `harvester/launcher/Priority.java`
  - File is dead code (no usages of getOrder())
  - Delete using: `rm` command or file deletion
  
  **Must NOT do**:
  - Do NOT modify PriorityJobLauncher (it doesn't use Priority enum)
  - Do NOT change any imports in harvester (already using common.queue.Priority)
  - Do NOT modify business logic or offset values

  **Parallelizable**: NO (depends on Task 0)

  **References**:
  
  **Current State**:
  - `common/queue/Priority.java:3-5` - Current simple enum (HIGH, LOW)
  - `common/queue/JobQueueService.java:13-14` - Static final constants to remove
  - `common/queue/JobQueueService.java:36-39` - calculateScore() method to modify
  - `harvester/launcher/Priority.java` - Dead code file to delete
  
  **Target Pattern** (from analysis):
  ```java
  // common/queue/Priority.java
  public enum Priority {
      HIGH(0L),
      LOW(1_000_000_000L);
      
      private final long offset;
      
      Priority(long offset) {
          this.offset = offset;
      }
      
      public long getOffset() {
          return offset;
      }
  }
  
  // common/queue/JobQueueService.java (calculateScore method)
  private double calculateScore(Instant scheduledAt, Priority priority) {
      return priority.getOffset() + scheduledAt.getEpochSecond();
  }
  ```
  
  **Why These Values**:
  - HIGH(0L): No offset, jobs execute at their scheduled time
  - LOW(1_000_000_000L): ~31 years offset, effectively "after all HIGH priority jobs"
  - Redis Sorted Set pops lowest score first → HIGH always executes before LOW

  **Acceptance Criteria**:
  
  **Compilation Verification**:
  - [ ] `./gradlew :common:compileJava -x test` → BUILD SUCCESSFUL
  - [ ] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL
  - [ ] `./gradlew :backend:compileJava -x test` → BUILD SUCCESSFUL
  
  **File Changes**:
  - [ ] `common/queue/Priority.java` modified (offset field added)
  - [ ] `common/queue/JobQueueService.java` modified (static final removed)
  - [ ] `harvester/launcher/Priority.java` deleted
  
  **Verification Commands**:
  ```bash
  # Check file exists
  ls harvester/src/main/java/io/swkoreatech/kosp/launcher/Priority.java
  # Expected: No such file or directory
  
  # Check Priority.java has getOffset()
  grep "getOffset" common/src/main/java/io/swkoreatech/kosp/common/queue/Priority.java
  # Expected: Found
  
  # Check JobQueueService uses getOffset()
  grep "priority.getOffset()" common/src/main/java/io/swkoreatech/kosp/common/queue/JobQueueService.java
  # Expected: Found
  
  # Check static finals removed
  grep "HIGH_PRIORITY_OFFSET" common/src/main/java/io/swkoreatech/kosp/common/queue/JobQueueService.java
  # Expected: No matches
  ```

  **Commit**: YES
  - Message: `refactor(common): unify Priority enum with single offset field`
  - Files: 
    - `common/src/main/java/io/swkoreatech/kosp/common/queue/Priority.java`
    - `common/src/main/java/io/swkoreatech/kosp/common/queue/JobQueueService.java`
    - `harvester/src/main/java/io/swkoreatech/kosp/launcher/Priority.java` (deleted)
  - Pre-commit: `./gradlew :common:compileJava :harvester:compileJava :backend:compileJava -x test`

---

- [ ] 2. Refactor CommitMiningStep.java (2 long methods)

  **What to do**:
  
  **Method 1**: `buildDocument()` (16줄 → ≤10줄)
  - Extract field mapping logic to helper methods
  - Group fields by category (basic info, metadata, timestamps)
  
  **Method 2**: `fetchAllCommits()` (25줄 → ≤10줄)
  - Extract pagination loop logic
  - Extract page processing logic (GraphQL call + error handling + save)
  
  **Must NOT do**:
  - Do NOT change field mapping logic or values
  - Do NOT modify GraphQL query structure
  - Do NOT change save logic or MongoDB document structure

  **Parallelizable**: NO (depends on Task 1)

  **References**:
  
  **Pattern References**:
  - `ScoreCalculationStep.java:219-248` - Similar helper method extraction pattern
  - `StatisticsAggregationStep.java:141-158` - Data fetching helper pattern
  
  **Type References**:
  - `collection/document/CommitDocument.java` - Document structure
  - `collection/graphql/response/RepositoryCommitsResponse.java` - GraphQL response type
  
  **Current Code Location**:
  - `CommitMiningStep.java:182-197` - buildDocument() method (16 lines)
  - `CommitMiningStep.java:119-143` - fetchAllCommits() method (25 lines)
  
  **KOSP Coding Rules Reference**:
  - `AGENTS.md` - Section "Strict Coding Rules" - Methods ≤10 lines

  **Acceptance Criteria**:
  
  **Compilation Verification**:
  - [ ] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL
  
  **Line Count Verification** (manual count using physical line methodology):
  - [ ] `buildDocument`: ≤10 lines
  - [ ] `fetchAllCommits`: ≤10 lines
  - [ ] All new helper methods: ≤10 lines
  
  **Isolation Verification**:
  - [ ] Git diff shows ONLY CommitMiningStep.java changed:
    ```bash
    git diff --name-only
    # Expected: harvester/src/main/java/.../CommitMiningStep.java (ONLY)
    ```

  **Commit**: YES
  - Message: `refactor(harvester): split long methods in CommitMiningStep`
  - Files: `harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/CommitMiningStep.java`
  - Pre-commit: `./gradlew :harvester:compileJava -x test` (must succeed)

---

- [ ] 3. Refactor RepositoryDiscoveryStep.java (2 long methods)

  **What to do**:
  
  **Method 1**: `saveRepositories()` (22줄 → ≤10줄)
  - Extract repository saving loop logic
  - Extract ContributedRepoDocument building logic
  
  **Method 2**: `fetchContributedRepos()` (11줄 → ≤10줄)
  - Extract error logging logic (already a pattern in other files)
  
  **Must NOT do**:
  - Do NOT change repository saving logic
  - Do NOT modify GraphQL response handling
  - Do NOT change context storage logic

  **Parallelizable**: NO (depends on Task 2)

  **References**:
  
  **Pattern References**:
  - `RepositoryDiscoveryStep.java:122-128` - logErrors() pattern (can extract)
  - `StatisticsAggregationStep.java:141-147` - Similar fetching pattern
  
  **Type References**:
  - `collection/document/ContributedRepoDocument.java` - Document structure
  - `collection/graphql/response/ContributedReposResponse.java` - GraphQL response
  
  **Current Code Location**:
  - `RepositoryDiscoveryStep.java:139-160` - saveRepositories() method (22 lines)
  - `RepositoryDiscoveryStep.java:110-120` - fetchContributedRepos() method (11 lines)

  **Acceptance Criteria**:
  
  **Compilation Verification**:
  - [ ] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL
  
  **Line Count Verification**:
  - [ ] `saveRepositories`: ≤10 lines
  - [ ] `fetchContributedRepos`: ≤10 lines
  - [ ] All new helper methods: ≤10 lines
  
  **Isolation Verification**:
  - [ ] Git diff shows ONLY RepositoryDiscoveryStep.java changed

  **Commit**: YES
  - Message: `refactor(harvester): split long methods in RepositoryDiscoveryStep`
  - Files: `harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/RepositoryDiscoveryStep.java`
  - Pre-commit: `./gradlew :harvester:compileJava -x test`

---

- [ ] 4. Refactor PullRequestMiningStep.java (1 long method)

  **What to do**:
  
  **Method 1**: `buildDocument()` (22줄 → ≤10줄)
  - Extract field mapping logic (basic fields, PR-specific fields, metadata)
  - Similar pattern to CommitMiningStep.buildDocument()
  
  **Must NOT do**:
  - Do NOT change PullRequestDocument field mappings
  - Do NOT modify PR data extraction logic

  **Parallelizable**: NO (depends on Task 3)

  **References**:
  
  **Pattern References**:
  - `PullRequestMiningStep.java:142-163` - buildDocument() method (22 lines)
  - `CommitMiningStep.java:182-197` - Similar buildDocument() pattern (after Task 2)
  
  **Type References**:
  - `collection/document/PullRequestDocument.java` - Document structure
  
  **Acceptance Criteria**:
  
  **Compilation Verification**:
  - [ ] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL
  
  **Line Count Verification**:
  - [ ] `buildDocument`: ≤10 lines
  - [ ] All new helper methods: ≤10 lines
  
  **Isolation Verification**:
  - [ ] Git diff shows ONLY PullRequestMiningStep.java changed

  **Commit**: YES
  - Message: `refactor(harvester): split long methods in PullRequestMiningStep`
  - Files: `harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/PullRequestMiningStep.java`
  - Pre-commit: `./gradlew :harvester:compileJava -x test`

---

- [ ] 5. Refactor IssueMiningStep.java (1 long method)

  **What to do**:
  
  **Method 1**: `buildDocument()` (14줄 → ≤10줄)
  - Extract field mapping logic
  - Simplest case (only 14 lines, need to reduce by 4)
  
  **Must NOT do**:
  - Do NOT change IssueDocument field mappings
  - Do NOT modify issue data extraction logic

  **Parallelizable**: NO (depends on Task 4)

  **References**:
  
  **Pattern References**:
  - `IssueMiningStep.java:142-155` - buildDocument() method (14 lines)
  - `PullRequestMiningStep.java:142-163` - Similar buildDocument() pattern (after Task 4)
  
  **Type References**:
  - `collection/document/IssueDocument.java` - Document structure

  **Acceptance Criteria**:
  
  **Compilation Verification**:
  - [ ] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL
  
  **Line Count Verification**:
  - [ ] `buildDocument`: ≤10 lines
  - [ ] All new helper methods: ≤10 lines
  
  **Isolation Verification**:
  - [ ] Git diff shows ONLY IssueMiningStep.java changed

  **Commit**: YES
  - Message: `refactor(harvester): split long methods in IssueMiningStep`
  - Files: `harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/IssueMiningStep.java`
  - Pre-commit: `./gradlew :harvester:compileJava -x test`

---

- [ ] 6. Verify CleanupStep.java (no refactoring needed)

  **What to do**:
  - Read file and verify all methods are ≤10 lines
  - Document verification in notepad
  - NO code changes (already compliant)
  
  **Must NOT do**:
  - Do NOT modify any code in CleanupStep.java
  - This is verification only

  **Parallelizable**: NO (depends on Task 5)

  **References**:
  - `CleanupStep.java` - File location

  **Acceptance Criteria**:
  
  **Line Count Verification**:
  - [ ] All methods verified ≤10 lines (manual count)
  - [ ] Verification documented in notepad
  
  **No Changes**:
  - [ ] `git diff --name-only` → Empty (no files changed)

  **Commit**: NO (no changes, just verification)

---

- [ ] 7. Final Verification and Documentation Update

  **What to do**:
  
  **Verification Commands**:
  1. **Ternary Check** (all 7 step files):
     ```bash
     ast_grep_search(
       pattern="$VAR ? $A : $B",
       lang="java",
       paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl"]
     )
     # Expected: 0 matches
     ```
  
  2. **Build Check** (all modules):
     ```bash
     ./gradlew :common:compileJava -x test
     ./gradlew :harvester:compileJava -x test
     ./gradlew :backend:compileJava -x test
     # Expected: ALL BUILD SUCCESSFUL
     ```
  
  3. **File Isolation Check**:
     ```bash
     git diff --name-only origin/refactor/kosp-compliance-phase1
     # Expected: Exactly these files:
     # - common/queue/Priority.java
     # - common/queue/JobQueueService.java
     # - harvester/collection/step/impl/CommitMiningStep.java
     # - harvester/collection/step/impl/RepositoryDiscoveryStep.java
     # - harvester/collection/step/impl/PullRequestMiningStep.java
     # - harvester/collection/step/impl/IssueMiningStep.java
     # (+ docs: 2 files)
     ```
  
  4. **Manual Line Count Audit**:
     - Open all 7 step files
     - Verify ALL methods ≤10 lines using physical line methodology
     - execute() method is exempt (framework entry point)
     - Document audit results in completion summary
  
  **Documentation Updates**:
  - Update `docs/todo/refactoring-issues.md`:
    - Mark Phase 2 complete (Priority enum + Harvester 전체)
    - Note remaining work (if any)
  
  - **CREATE NEW** summary file `.sisyphus/notepads/harvester-redis-scheduler/phase2-completion.md`:
    ```markdown
    # Phase 2 Completion Summary
    
    ## Violations Fixed
    - Priority enum: 중복 제거, offset 통합
    - Harvester Step: 6개 긴 메서드 → 0개
    
    ## Files Modified
    1. Common: Priority.java, JobQueueService.java
    2. Harvester: 4개 Step 파일 (Commit, Repository, PullRequest, Issue)
    3. Deleted: harvester/launcher/Priority.java
    
    ## Commits
    - refactor/kosp-compliance-phase2 (6-7 commits)
    
    ## Verification Evidence
    - AST grep: 0 ternaries (harvester 전체 7개 파일)
    - Build: ALL SUCCESSFUL (common, harvester, backend)
    - KOSP compliance: 100% (모든 메서드 ≤10줄)
    ```
  
  **Must NOT do**:
  - Do NOT merge to main (user decides when to merge)
  - Do NOT start Phase 3 without user approval

  **Parallelizable**: NO (depends on Task 1-6 completion)

  **References**:
  - `docs/todo/refactoring-issues.md` - Issue tracker to update
  - `.sisyphus/plans/code-quality-refactoring.md` - Phase 1 plan reference

  **Acceptance Criteria**:
  
  **Zero Ternaries**:
  - [ ] AST grep finds 0 ternaries in all 7 step files
  
  **Build Success** (all modules):
  - [ ] `./gradlew :common:compileJava -x test` → BUILD SUCCESSFUL
  - [ ] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL
  - [ ] `./gradlew :backend:compileJava -x test` → BUILD SUCCESSFUL
  
  **File Isolation**:
  - [ ] Changed files count: 6 .java files (3 common + 4 harvester, 1 deleted)
  - [ ] Documentation: 2 .md files
  - [ ] Total: 8 files (6 code + 2 docs)
  
  **Line Counts Verified**:
  - [ ] Manual audit confirms all methods ≤10 lines (execute() exempt)
  
  **Documentation Updated**:
  - [ ] `docs/todo/refactoring-issues.md` updated
  - [ ] `phase2-completion.md` newly created

  **Commit**: YES
  - Message: `docs: mark Phase 2 KOSP compliance complete (Harvester 전체 + Priority enum)`
  - Files: `docs/todo/refactoring-issues.md`, `.sisyphus/notepads/harvester-redis-scheduler/phase2-completion.md`
  - Pre-commit: None (documentation only)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `refactor(common): unify Priority enum with single offset field` | Priority.java, JobQueueService.java, Priority.java (deleted) | 3-module build |
| 2 | `refactor(harvester): split long methods in CommitMiningStep` | CommitMiningStep.java | harvester build |
| 3 | `refactor(harvester): split long methods in RepositoryDiscoveryStep` | RepositoryDiscoveryStep.java | harvester build |
| 4 | `refactor(harvester): split long methods in PullRequestMiningStep` | PullRequestMiningStep.java | harvester build |
| 5 | `refactor(harvester): split long methods in IssueMiningStep` | IssueMiningStep.java | harvester build |
| 6 | (No commit - verification only) | - | - |
| 7 | `docs: mark Phase 2 KOSP compliance complete (Harvester 전체 + Priority enum)` | refactoring-issues.md, phase2-completion.md | Manual review |

---

## Success Criteria

### Verification Commands
```bash
# 1. Ternary check (MUST return 0)
ast_grep_search(
  pattern="$VAR ? $A : $B",
  lang="java",
  paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl"]
)

# 2. Build check (MUST all succeed)
./gradlew :common:compileJava -x test
./gradlew :harvester:compileJava -x test
./gradlew :backend:compileJava -x test

# 3. File isolation (MUST show exactly 8 files: 6 code + 2 docs)
git diff --name-only origin/refactor/kosp-compliance-phase1
```

### Final Checklist
- [ ] Priority enum unified (common only, offset field)
- [ ] harvester/launcher/Priority.java deleted
- [ ] All 6 long methods split (Harvester Step 4개 파일)
- [ ] CleanupStep.java verified (no changes)
- [ ] All ternaries removed (0 found)
- [ ] All methods ≤10 lines (execute() exempt)
- [ ] All builds succeed (common, harvester, backend)
- [ ] Documentation updated (2 .md files)
