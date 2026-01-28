# Code Quality Refactoring - Phase 1 (KOSP Compliance)

## Context

### Original Request
Fix KOSP coding rule violations in Harvester step implementations (Priority 3 from `docs/todo/refactoring-issues.md`):
- Remove all ternary operators (`? :`)
- Ensure all methods ≤ 10 lines
- Maintain zero business logic changes

### Interview Summary

**Key Discussions**:
- User completed Priority 1 & 2 (Redis scheduler refactoring) successfully (9 commits)
- Background agents analyzed 7 step files and found violations in 2 files
- User chose to scope Phase 1 tightly: fix coding rules only, postpone utilities and optimization
- User requested full work plan with TDD-level rigor (despite no test changes needed)

**Research Findings**:
- AST grep validated: **Exactly 9 ternary operators** (5 in ScoreCalculationStep, 4 in StatisticsAggregationStep)
- Manual analysis found: **9 methods >10 lines** (4 in ScoreCalculationStep, 5 in StatisticsAggregationStep)
- Largest method: `aggregateFromMongoDB()` at 47 lines
- No `else`/`else-if` violations found ✅
- Build compiles successfully (baseline captured to `.sisyphus/baseline-build.log`)

### Metis Review

**Identified Gaps** (addressed):

1. **Verification Strategy Gap** → **Resolved**: Tests have pre-existing compilation errors unrelated to refactoring. Use compile-time + AST grep verification instead.

2. **Violation Inventory Imprecise** → **Resolved**: AST grep confirmed exact count (9 ternaries). Manual review documented all methods >10 lines with current line counts.

3. **Incremental Verification Missing** → **Resolved**: Verify after EACH file (not batched). Commands specified in TODOs.

4. **Scope Creep Vectors** → **Guardrails Added**: Explicit "MUST NOT" list prevents AI-slop (no utilities, no test touching, no style changes beyond rules).

5. **Rollback Strategy Missing** → **Resolved**: Commit after each file allows targeted revert. Git diff verification ensures only 2 files changed.

6. **Edge Case Handling Undefined** → **Resolved**: 
   - Stream ternaries → Extract to method reference
   - Line count methodology → Physical lines (simple to verify)
   - Parameter limit → Max 4 per extracted method

---

## Work Objectives

### Core Objective
Refactor 2 Harvester step implementations to comply with KOSP coding rules (no ternaries, methods ≤10 lines) while preserving 100% of business logic behavior.

### Concrete Deliverables
- **ScoreCalculationStep.java**: 5 ternaries removed, 4 methods split → all methods ≤10 lines
- **StatisticsAggregationStep.java**: 4 ternaries removed, 5 methods split → all methods ≤10 lines

### Definition of Done (Core Refactoring)
- [x] AST grep returns 0 ternary operators: `ast_grep_search(pattern="$VAR ? $A : $B", lang="java")`
- [x] All methods ≤10 lines (manual count using physical line methodology below)
- [x] Build succeeds: `./gradlew :harvester:compileJava -x test`
- [x] Source diff shows ONLY 2 .java files changed (documentation updates are separate in Task 3)
- [x] No business logic behavior changed

### Line Counting Methodology (Objective Standard)
**Definition**: Physical lines from opening `{` to closing `}` of method body, including:
- ✅ Multi-line method signatures (count only body lines)
- ✅ Blank lines within method
- ✅ Comments within method
- ✅ Chained calls split across lines (each line counts)
- ✅ Multi-line string literals

**Example**:
```java
private BigDecimal calculate(    // ← NOT counted (signature)
    int param1,                   // ← NOT counted (signature)
    int param2) {                 // ← Line 1 (opening brace)
                                  // ← Line 2 (blank line)
    if (param1 == 0) {            // ← Line 3
        return BigDecimal.ZERO;   // ← Line 4
    }                             // ← Line 5
    return BigDecimal            // ← Line 6
        .valueOf(param1)          // ← Line 7
        .add(BigDecimal.valueOf(param2)); // ← Line 8
}                                 // ← Line 9 (closing brace)
// Total: 9 lines (≤10 ✅)
```

**Tool for Verification**:
```bash
# Count lines between method signature and closing brace
grep -A 50 "private.*methodName" File.java | head -n 20
# Manually count from opening { to closing }
```

### Must Have
- Early return pattern for all ternary replacements
- Private helper methods for extractions (not package-private)
- Follow existing naming conventions: `calculate*`, `get*OrZero`, `count*`, `find*`
- Compilation verification after EACH file
- Atomic commits per file

### Must NOT Have (Guardrails)
- **No utility extraction**: Cannot create shared helper classes or move code to common package (Phase 2 scope)
- **No test modifications**: Even if test compilation errors are visible, do NOT touch test files
- **No source scope expansion**: Source code changes limited to ScoreCalculationStep.java and StatisticsAggregationStep.java ONLY (documentation updates in Task 3 are separate)
- **No algorithm changes**: Preserve exact calculation logic and data flow
- **No renaming**: Keep existing variable/method names unless extraction requires new names
- **No style improvements**: Fix only ternaries and line counts, not abbreviations/imports/docs
- **No "while I'm here" fixes**: Resist temptation to optimize or refactor beyond stated goals

---

## Verification Strategy (MANDATORY)

> **CRITICAL**: Tests have pre-existing compilation errors unrelated to this work.
> User confirmed these errors are out of scope. DO NOT attempt to fix tests.

### Verification Approach

**Baseline Established** (before refactoring):
```bash
./gradlew :harvester:compileJava -x test
# Output: BUILD SUCCESSFUL in 1s
# Saved to: .sisyphus/baseline-build.log
```

**After Each File**:
1. **Compilation**: `./gradlew :harvester:compileJava -x test` → must succeed
2. **Ternary Check** (use EITHER method):
   - **Method A (OpenCode environment, preferred)**: Use `ast_grep_search` tool (NOT a CLI command):
     ```
     ast_grep_search(pattern="$VAR ? $A : $B", lang="java", paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/ScoreCalculationStep.java"])
     ```
     Expected: 0 matches
   - **Method B (CLI fallback, may have false positives)**: 
     ```bash
     grep -E '\?\s.*\s:' harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/ScoreCalculationStep.java | grep -v '//' | grep -v '^\s*/\*'
     ```
     Expected: 0 matches (manually verify any results aren't in strings or block comments)
3. **Manual Review**: Count lines in modified methods using physical line methodology → all ≤10
4. **Git Diff Check**: `git diff --name-only | grep '\.java$'` → Shows only ScoreCalculationStep.java

**Final Verification** (after Task 1 & 2 complete, before Task 3):
1. **Ternary Check** (use EITHER method):
   - **OpenCode AST grep tool** (preferred):
     ```
     ast_grep_search(pattern="$VAR ? $A : $B", lang="java", paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl"])
     ```
     Expected: 0 matches across both files
   - **CLI grep fallback** (may have false positives - manually verify any matches):
     ```bash
     grep -rE '\?\s.*\s:' harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/ScoreCalculationStep.java harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/StatisticsAggregationStep.java | grep -v '//' | grep -v '^\s*/\*'
     ```
     Expected: 0 matches
2. **Build**: `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL
3. **Source Diff**: `git diff --name-only | grep '\.java$'` → Shows exactly 2 files:
   - `harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/ScoreCalculationStep.java`
   - `harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/StatisticsAggregationStep.java`
4. **Manual Line Count**: All methods ≤10 lines in both files (using physical line methodology)

**Final Verification** (after Task 3 documentation updates):
5. **Documentation Diff**: `git diff --name-only | grep '\.md$'` → Shows exactly 2 files:
   - `docs/todo/refactoring-issues.md` (updated)
   - `.sisyphus/notepads/harvester-redis-scheduler/phase1-completion.md` (newly created)
6. **Total Changes**: `git diff --name-only` → Shows exactly 4 files total (2 .java + 2 .md)

---

## Task Flow

```
Task 0 (Setup) → Task 1 (ScoreCalculationStep) → Task 2 (StatisticsAggregationStep) → Task 3 (Final Verification)
```

**No parallelization**: Tasks must run sequentially. Each task depends on previous verification.

---

## TODOs

> **Implementation Pattern**: Zero business logic changes. Extract → Verify → Commit.

---

- [x] 0. Setup: Create feature branch and verify baseline

  **What to do**:
  - Create feature branch: `git checkout -b refactor/kosp-compliance-phase1`
  - Verify current build state: `./gradlew :harvester:compileJava -x test` → expect SUCCESS
  - Verify baseline ternary count: AST grep should find 9 total

  **Parallelizable**: NO (foundation for all tasks)

  **References**:
  - `.sisyphus/baseline-build.log` - Pre-captured build output
  - `AGENTS.md` - KOSP Backend coding rules and project patterns
  - `harvester/AGENTS.md` - Harvester module specifics
  - `.sisyphus/notepads/harvester-redis-scheduler/refactoring-plan.md` - Refactoring patterns and ternary locations

  **Acceptance Criteria**:
  - [x] Branch created: `git branch --show-current` → `refactor/kosp-compliance-phase1`
  - [x] Build succeeds: `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL
  - [x] Baseline verified: AST grep finds exactly 9 ternaries (5 in ScoreCalculationStep, 4 in StatisticsAggregationStep)

  **Commit**: NO (no changes yet)

---

- [x] 1. Refactor ScoreCalculationStep.java (5 ternaries + 4 long methods)

  **What to do**:
  
  **Part A: Remove 5 Ternary Operators**
  
  1. **Line 173**: `return hasHighStarRepo ? BigDecimal.valueOf(2) : BigDecimal.ZERO;`
     - Extract to: `private BigDecimal getHighStarRepoBonus(boolean hasHighStarRepo)`
     - Pattern: `if (!hasHighStarRepo) return BigDecimal.ZERO; return BigDecimal.valueOf(2);`
  
  2. **Line 181**: `return hasMergedPrToHighStarRepo ? BigDecimal.valueOf(1.5) : BigDecimal.ZERO;`
     - Extract to: `private BigDecimal getHighStarPrBonus(boolean hasMergedPrToHighStarRepo)`
  
  3. **Line 187**: Stream ternary `.mapToInt(pr -> pr.getClosedIssuesCount() != null ? pr.getClosedIssuesCount() : 0)`
     - Extract to: `private int getClosedIssuesCountOrZero(PullRequestDocument pr)`
     - Replace stream: `.mapToInt(this::getClosedIssuesCountOrZero)`
  
  4. **Line 190**: `return totalClosedIssues >= CLOSED_ISSUES_THRESHOLD ? BigDecimal.valueOf(1) : BigDecimal.ZERO;`
     - Extract to: `private BigDecimal getClosedIssuesBonus(int totalClosedIssues)`
  
  5. **Line 198**: `return hasCrossRepoPrMerged ? BigDecimal.valueOf(0.5) : BigDecimal.ZERO;`
     - Extract to: `private BigDecimal getCrossRepoPrBonus(boolean hasCrossRepoPrMerged)`
  
  **Part B: Split 4 Long Methods**
  
  1. **calculateActivityScore()** (24 lines → ≤10 lines)
     - Extract commit counting loop to: `private void countCommitsPerRepo(List<CommitDocument> commits, Map<String, RepoStats> statsMap)`
     - Extract PR counting loop to: `private void countPrsPerRepo(List<PullRequestDocument> prs, Map<String, RepoStats> statsMap)`
     - Extract max score calculation to: `private int findMaxActivityScore(Map<String, RepoStats> statsMap)`
     - Result: `calculateActivityScore` becomes ~8 lines (fetch → count commits → count PRs → find max)
  
  2. **calculateRepoActivityScore()** (12 lines → ≤10 lines)
     - Current structure: 4 if-blocks for threshold checks (lines 125-136)
     - **Refactor approach**: Extract predicate helpers to reduce line count
       - Extract: `private boolean hasHighActivity(int commitCount, int prCount)` → returns `commitCount >= 100 && prCount >= 20`
       - Extract: `private boolean hasMediumActivity(int commitCount, int prCount)` → returns `commitCount >= 30 && prCount >= 5`
       - Extract: `private boolean hasLowActivity(int commitCount, int prCount)` → returns `commitCount >= 5 || prCount >= 1`
       - Rewrite `calculateRepoActivityScore` to:
         ```java
         if (hasHighActivity(commitCount, prCount)) return 3;
         if (hasMediumActivity(commitCount, prCount)) return 2;
         if (hasLowActivity(commitCount, prCount)) return 1;
         return 0;
         ```
     - **Result**: ≤10 lines (≤5 lines in main method + 3 one-liner helpers)
     - **Target**: ≤10 lines (hard requirement, no exceptions)
  
  3. **calculateDiversityScore()** (15 lines → ≤10 lines)
     - Extract tier calculation to: `private BigDecimal getDiversityScoreForCount(int repoCount)`
     - Result: `calculateDiversityScore` becomes ~3 lines (fetch repos → get count → calculate tier)
  
  4. **calculateImpactScore()** (13 lines → ≤10 lines)
     - Current: Lines 154-166 (2 list fetches + 4 bonus calculations + 4 additions + min cap = 13 lines)
     - **Refactor approach**: Extract calculation logic to helper overload
       - Create helper: `private BigDecimal calculateImpactScore(List<ContributedRepoDocument> repos, List<PullRequestDocument> prs)`
       - Move all bonus calculations to the helper
       - Public `calculateImpactScore(Long userId)` becomes orchestration only:
         ```java
         List<ContributedRepoDocument> repos = repoRepository.findByUserId(userId);
         List<PullRequestDocument> prs = prRepository.findByUserId(userId);
         return calculateImpactScore(repos, prs);
         ```
     - **Result**: Public method ≤5 lines, helper ≤10 lines
     - **Target**: Both methods ≤10 lines (hard requirement, no exceptions)
  
  **Must NOT do**:
  - Do NOT create shared utility classes (Phase 2 scope)
  - Do NOT rename existing methods/variables beyond extraction needs
  - Do NOT change calculation algorithms or thresholds
  - Do NOT fix unrelated issues (imports, abbreviations, docs)
  - Do NOT modify test files

  **Parallelizable**: NO (depends on Task 0)

  **References**:

  **Pattern References** (existing code to follow):
  - `ScoreCalculationStep.java:121-123` - Simple helper method pattern (`buildRepoKey`)
  - `ScoreCalculationStep.java:125-136` - Multi-threshold scoring pattern (reference for splitting if needed)

  **Type References**:
  - `ScoreCalculationStep.java:209-212` - `RepoStats` inner class (used in `calculateActivityScore`)
  - `collection/document/PullRequestDocument.java` - `getClosedIssuesCount()` method signature
  - `collection/document/CommitDocument.java` - Document structure for stream operations

  **Naming Conventions to Follow**:
  - Bonus methods: `get*Bonus(boolean/int condition)` → BigDecimal
  - Null-safe getters: `get*OrZero(Document doc)` → int
  - Calculation helpers: `calculate*`, `find*`, `count*`

  **KOSP Coding Rules Reference**:
  - `AGENTS.md` - Section "Strict Coding Rules" - No ternaries, early returns, methods ≤10 lines

  **Acceptance Criteria**:

  **Compilation Verification**:
  - [x] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL

  **Ternary Removal Verification**:
  - [x] AST grep on ScoreCalculationStep.java finds ZERO ternaries:
    ```bash
    ast_grep_search(
      pattern="$VAR ? $A : $B", 
      lang="java", 
      paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/ScoreCalculationStep.java"]
    )
    # Expected: 0 matches
    ```

  **Line Count Verification** (manual count using physical line methodology):
  - [x] `calculateActivityScore`: ≤10 lines
  - [x] `calculateRepoActivityScore`: ≤10 lines
  - [x] `calculateDiversityScore`: ≤10 lines
  - [x] `calculateImpactScore`: ≤10 lines
  - [x] All new helper methods: ≤10 lines

  **Isolation Verification**:
  - [x] Git diff shows ONLY ScoreCalculationStep.java changed:
    ```bash
    git diff --name-only
    # Expected: harvester/src/main/java/.../ScoreCalculationStep.java (ONLY)
    ```

  **Commit**: YES
  - Message: `refactor(harvester): remove ternaries and split long methods in ScoreCalculationStep`
  - Files: `harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/ScoreCalculationStep.java`
  - Pre-commit: `./gradlew :harvester:compileJava -x test` (must succeed)

---

- [x] 2. Refactor StatisticsAggregationStep.java (4 ternaries + 5 long methods)

  **What to do**:
  
  **Part A: Remove 4 Stream Ternary Operators**
  
  1. **Line 103**: `.mapToInt(c -> c.getAdditions() != null ? c.getAdditions() : 0)`
     - Extract to: `private int getAdditionsOrZero(CommitDocument commit)`
     - Replace: `.mapToInt(this::getAdditionsOrZero)`
  
  2. **Line 107**: `.mapToInt(c -> c.getDeletions() != null ? c.getDeletions() : 0)`
     - Extract to: `private int getDeletionsOrZero(CommitDocument commit)`
     - Replace: `.mapToInt(this::getDeletionsOrZero)`
  
  3. **Line 120**: `.mapToInt(r -> r.getStargazersCount() != null ? r.getStargazersCount() : 0)`
     - Extract to: `private int getStargazersCountOrZero(ContributedRepoDocument repo)`
     - Replace: `.mapToInt(this::getStargazersCountOrZero)`
  
  4. **Line 125**: `.mapToInt(r -> r.getForksCount() != null ? r.getForksCount() : 0)`
     - Extract to: `private int getForksCountOrZero(ContributedRepoDocument repo)`
     - Replace: `.mapToInt(this::getForksCountOrZero)`
  
  **Part B: Split 5 Long Methods**
  
  1. **aggregateFromMongoDB()** (47 lines → ≤10 lines) **[CRITICAL - Largest Method]**
     - Extract to separate calculation methods:
       - `private int calculateTotalAdditions(List<CommitDocument> commits)`
       - `private int calculateTotalDeletions(List<CommitDocument> commits)`
       - `private int calculateNightCommits(List<CommitDocument> commits)`
       - `private int calculateOwnedReposCount(List<ContributedRepoDocument> repos)`
       - `private int calculateTotalStars(List<ContributedRepoDocument> repos)`
       - `private int calculateTotalForks(List<ContributedRepoDocument> repos)`
     - Result: `aggregateFromMongoDB` becomes ~8 lines:
       ```java
       fetch 4 lists → call 6 calc methods → return new AggregatedStats(results)
       ```
  
  2. **saveToMySQL()** (25 lines → ≤10 lines)
     - Extract statistics update to: `private void updateStatisticsFields(GithubUserStatistics stats, AggregatedStats aggregated)`
     - Result: `saveToMySQL` becomes ~5 lines (get entity → update fields → update period → save)
  
  3. **updateContributedRepoStats()** (12 lines → ≤10 lines)
     - Current: Lines 178-189 (4 repo fetches + for-loop + saveAll = 12 lines)
     - **Refactor approach**: Extract data fetching to helper
       - Create record: `private record RepoData(List<ContributedRepoDocument> repos, List<CommitDocument> commits, List<PullRequestDocument> prs, List<IssueDocument> issues)`
       - Extract: `private RepoData fetchRepoData(Long userId)` → fetches all 4 lists
       - Extract: `private void updateAllRepoStats(List<ContributedRepoDocument> repos, RepoData data)` → loop + updateSingleRepoStats calls
       - Rewrite `updateContributedRepoStats`:
         ```java
         RepoData data = fetchRepoData(userId);
         updateAllRepoStats(data.repos(), data);
         repoDocumentRepository.saveAll(data.repos());
         ```
     - **Result**: Main method ≤4 lines
     - **Target**: ≤10 lines (hard requirement, no exceptions)
  
  4. **updateSingleRepoStats()** (15 lines → ≤10 lines)
     - Current: Lines 191-205 (1 local var + 4 count calls + 1 update call)
     - **Refactor approach**: Extract counting block to helper
       - `private RepoContributionStats countAllForRepo(String repoFullName, List<CommitDocument> commits, List<PullRequestDocument> prs, List<IssueDocument> issues)`
       - Returns record with `commitCount`, `prCount`, `issueCount`, `lastCommit`
     - **Target**: ≤10 lines (no exceptions)
  
  5. **findLastCommitDate()** (8 lines → KEEP AS-IS)
     - Current: Lines 225-232 (single stream chain with filter-map-max-orElse)
     - **Already compliant**: ≤10 lines (8 lines), logically cohesive single-purpose method
     - **No refactor needed**: Maintains as-is
  
  **Must NOT do**:
  - Do NOT create StreamUtils or NullSafeMapper utilities (Phase 2)
  - Do NOT refactor the `AggregatedStats` record
  - Do NOT change statistical calculation logic
  - Do NOT modify MongoDB repository methods
  - Do NOT touch test files

  **Parallelizable**: NO (depends on Task 1 completion)

  **References**:

  **Pattern References** (existing code to follow):
  - `StatisticsAggregationStep.java:110-112` - Stream filter + count pattern (clean, reuse for extractions)
  - `StatisticsAggregationStep.java:144-150` - Null-safe helper method pattern (`isNightCommit`)
  - `StatisticsAggregationStep.java:207-211` - Simple filtering + counting pattern (`countCommitsForRepo`)

  **Type References**:
  - `collection/document/CommitDocument.java` - `getAdditions()`, `getDeletions()`, `getAuthoredAt()`
  - `collection/document/ContributedRepoDocument.java` - `getStargazersCount()`, `getForksCount()`, `getIsOwner()`
  - `StatisticsAggregationStep.java:234-247` - `AggregatedStats` record structure (12 fields)
  - `common/github/model/GithubUserStatistics.java` - `updateStatistics()` method signature

  **Naming Conventions to Follow**:
  - Null-safe getters: `get*OrZero(Document doc)` → int
  - Calculation methods: `calculate*` → int
  - Counting methods: `count*` → int
  - Record/data classes: `*Data`, `*Counts` (if creating intermediate holders)

  **Acceptance Criteria**:

  **Compilation Verification**:
  - [x] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL

  **Ternary Removal Verification**:
  - [x] AST grep on StatisticsAggregationStep.java finds ZERO ternaries:
    ```bash
    ast_grep_search(
      pattern="$VAR ? $A : $B", 
      lang="java", 
      paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/StatisticsAggregationStep.java"]
    )
    # Expected: 0 matches
    ```

  **Line Count Verification** (manual count using physical line methodology):
  - [x] `aggregateFromMongoDB`: ≤10 lines
  - [x] `saveToMySQL`: ≤10 lines
  - [x] `updateContributedRepoStats`: ≤10 lines
  - [x] `updateSingleRepoStats`: ≤10 lines
  - [x] `findLastCommitDate`: ≤10 lines (currently 8, expected to stay unchanged)
  - [x] All new helper methods: ≤10 lines

  **Isolation Verification**:
  - [x] Git diff shows ONLY StatisticsAggregationStep.java changed:
    ```bash
    git diff --name-only
    # Expected: harvester/src/main/java/.../StatisticsAggregationStep.java (ONLY)
    ```

  **Commit**: YES
  - Message: `refactor(harvester): remove ternaries and split long methods in StatisticsAggregationStep`
  - Files: `harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl/StatisticsAggregationStep.java`
  - Pre-commit: `./gradlew :harvester:compileJava -x test` (must succeed)

---

- [x] 3. Final Verification and Documentation Update

  **What to do**:
  
  **Verification Commands**:
  1. **Ternary Check** (both files):
     ```bash
     ast_grep_search(
       pattern="$VAR ? $A : $B", 
       lang="java", 
       paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl"]
     )
     # Expected: 0 matches
     ```
  
  2. **Build Check**:
     ```bash
     ./gradlew :harvester:compileJava -x test
     # Expected: BUILD SUCCESSFUL
     ```
  
  3. **File Isolation Check**:
     ```bash
     git diff --name-only
     # Expected: EXACTLY these 2 files:
     # harvester/src/main/java/.../ScoreCalculationStep.java
     # harvester/src/main/java/.../StatisticsAggregationStep.java
     ```
  
  4. **Manual Line Count Audit**:
     - Open both files in editor
     - Verify ALL methods (including new helpers) ≤10 lines using physical line methodology
     - All methods MUST be ≤10 lines (no exceptions - this is a hard KOSP requirement)
  
  **Documentation Updates**:
  - Update `docs/todo/refactoring-issues.md` (already exists):
    - Mark Priority 3, Issue #1 (ternary operators) as ✅ COMPLETED
    - Mark Priority 3, Issue #6 (method length) as ✅ COMPLETED (for 2 files)
    - Note: Remaining 5 step files (RepositoryDiscovery, PRMining, IssueMining, CommitMining, Cleanup) postponed to Phase 1B or future work
  
  - **CREATE NEW** summary file `.sisyphus/notepads/harvester-redis-scheduler/phase1-completion.md`:
    ```markdown
    # Phase 1 Completion Summary
    
    ## Violations Fixed
    - Ternary operators: 9 → 0 (100% compliance)
    - Methods >10 lines: 9 → 0 (in 2 target files)
    
    ## Files Refactored
    1. ScoreCalculationStep.java
    2. StatisticsAggregationStep.java
    
    ## Commits
    - refactor/kosp-compliance-phase1 (3 commits)
    
    ## Verification Evidence
    - AST grep: 0 ternaries
    - Build: SUCCESS
    - Git diff: 2 files only
    - Line counts: [attach manual audit results]
    ```

  **Must NOT do**:
  - Do NOT merge to main (user decides when to merge)
  - Do NOT proceed to Phase 2 (utilities) without user approval
  - Do NOT update AGENTS.md or other documentation beyond issue tracker

  **Parallelizable**: NO (depends on Task 1 & 2 completion)

  **References**:
  - `docs/todo/refactoring-issues.md` - Issue tracker to update
  - `AGENTS.md` - KOSP Backend coding rules
  - `harvester/AGENTS.md` - Harvester module specifics
  - `.sisyphus/notepads/harvester-redis-scheduler/refactoring-plan.md` - Original refactoring analysis

  **Acceptance Criteria**:

  **Zero Ternaries**:
  - [x] AST grep finds 0 ternaries in both files

  **Build Success**:
  - [x] `./gradlew :harvester:compileJava -x test` → BUILD SUCCESSFUL

  **File Isolation** (source code):
  - [x] Source diff shows exactly 2 .java files: `git diff --name-only | grep '\\.java$'`
    - `harvester/.../ScoreCalculationStep.java`
    - `harvester/.../StatisticsAggregationStep.java`

  **Documentation Changes** (Task 3 only):
  - [x] Documentation diff shows exactly 2 .md files: `git diff --name-only | grep '\\.md$'`
    - `docs/todo/refactoring-issues.md` (updated)
    - `.sisyphus/notepads/harvester-redis-scheduler/phase1-completion.md` (newly created)
  
  **Total Changes**:
  - [x] `git diff --name-only` → Shows exactly 4 files total (2 .java + 2 .md)

  **Line Counts Verified**:
  - [x] Manual audit confirms all methods ≤10 lines (hard requirement, no exceptions)

  **Documentation Updated**:
  - [x] `docs/todo/refactoring-issues.md` updated (Priority 3 items marked complete)
  - [x] Completion summary **newly created** at `.sisyphus/notepads/harvester-redis-scheduler/phase1-completion.md`

  **Commit**: YES
  - Message: `docs: mark Phase 1 KOSP compliance refactoring as complete`
  - Files: `docs/todo/refactoring-issues.md`, `.sisyphus/notepads/harvester-redis-scheduler/phase1-completion.md`
  - Pre-commit: None (documentation only)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `refactor(harvester): remove ternaries and split long methods in ScoreCalculationStep` | ScoreCalculationStep.java | `./gradlew :harvester:compileJava -x test` + AST grep |
| 2 | `refactor(harvester): remove ternaries and split long methods in StatisticsAggregationStep` | StatisticsAggregationStep.java | `./gradlew :harvester:compileJava -x test` + AST grep |
| 3 | `docs: mark Phase 1 KOSP compliance refactoring as complete` | refactoring-issues.md, phase1-completion.md | Manual review |

---

## Success Criteria

### Verification Commands
```bash
# Ternary check (MUST return 0)
ast_grep_search(pattern="$VAR ? $A : $B", lang="java", paths=["harvester/src/main/java/io/swkoreatech/kosp/collection/step/impl"])

# Build check (MUST succeed)
./gradlew :harvester:compileJava -x test

# File isolation (MUST show exactly 2 .java files + 2 .md docs)
git diff --name-only
```

### Final Checklist
- [x] All "Must Have" present (early returns, private methods, naming conventions, atomic commits)
- [x] All "Must NOT Have" absent (no utilities, no tests, no scope expansion, no algorithm changes)
- [x] All 9 ternaries removed (AST grep verified)
- [x] All 9 long methods split (manual count verified)
- [x] Build compiles successfully
- [x] Only 2 source files changed (git diff verified)
