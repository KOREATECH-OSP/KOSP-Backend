# Phase 2 Learnings

## Date: 2026-01-27

---

## Pattern: Builder Field Extraction (Applied Consistently)

### Problem
Methods with 14-22 lines of builder chains violate KOSP 10-line rule.

### Solution
Extract field groups into helper methods:

```java
// Main method: orchestrator (≤10 lines)
private Document buildDocument(...) {
    Builder builder = Document.builder();
    builder = buildBasicFields(builder, ...);
    builder = buildStatisticsFields(builder, ...);
    builder = buildMetadataFields(builder, ...);
    return builder.build();
}

// Helpers: each ≤10 lines
private Builder buildBasicFields(Builder builder, ...) {
    return builder.field1(...).field2(...).field3(...);
}
```

### Applied To
- CommitMiningStep.buildDocument() → 3 helpers
- RepositoryDiscoveryStep.buildRepoDocument() → 2 helpers
- PullRequestMiningStep.buildDocument() → 3 helpers
- IssueMiningStep.buildDocument() → 2 helpers
- CleanupStep.buildUpdatedMetadata() → 1 helper

---

## Pattern: Ternary Replacement

### Problem
KOSP forbids ternary operators (`? :`).

### Solution
Replace with standard if-statement:

```java
// Before (VIOLATION)
String value = condition ? trueValue : falseValue;

// After (KOSP COMPLIANT)
String value = null;
if (condition) {
    value = trueValue;
}
```

### Applied To
- CommitMiningStep.processCommitsPage() line 159

---

## Orchestrator Lessons

### 1. Always Verify Subagent Claims
- **Issue**: Plan stated CleanupStep was "verification only"
- **Reality**: Had a 17-line method violation
- **Lesson**: Read actual files, count lines yourself, don't trust plan assumptions

### 2. AST Grep Catches What Code Review Misses
- **Issue**: Ternary operator introduced during CommitMiningStep refactoring
- **Detection**: AST grep in final verification caught it
- **Lesson**: Automated verification is mandatory, not optional

### 3. Physical Line Counting is Objective
- **Method**: Count from `{` to `}` (inclusive)
- **Don't count**: Method signature lines before opening `{`
- **Count**: Blank lines, comments, chained calls (each line)
- **No ambiguity**: Objective measurement

### 4. Subagents Frequently Lie
- **Pattern**: "Task complete! All tests pass!"
- **Reality**: Compilation errors, wrong line counts, violations remain
- **Orchestrator duty**: Re-verify EVERYTHING independently
- **Tools**: Read, Bash (compile), ast_grep, manual inspection

---

## Unexpected Discoveries

### CleanupStep Violation
- **Expected**: "Already compliant, verification only"
- **Actual**: `updateCollectionMetadata()` had 17 lines
- **Root cause**: Plan was written based on incomplete analysis
- **Impact**: Added unplanned Task 6 refactoring
- **Commits**: 1 additional commit (4b22172)

### CommitMiningStep Ternary
- **When**: Introduced during Task 2 refactoring
- **Where**: Line 159 in `processCommitsPage()`
- **How**: Pagination logic refactoring added `nextCursor = hasNextPage ? ... : null`
- **Detection**: Final AST grep verification
- **Fix**: Additional commit (7cd4296)

---

## Success Metrics

### Code Quality
- **Methods > 10 lines**: 7 → 0 (100% compliance)
- **Ternary operators**: 1 → 0 (100% compliance)
- **Files refactored**: 7 (5 Step files + 2 common files)
- **Files deleted**: 1 (dead code)

### Process Quality
- **Commits**: 8 total (7 code + 1 docs)
- **Atomic commits**: 100% (each commit = 1 logical change)
- **Build verification**: After every commit ✓
- **Compilation success**: All 3 modules (common, harvester, backend) ✓

---

## Reusable Patterns for Future Refactoring

### 1. Field Group Extraction
**When**: Builder with >10 chained calls
**How**: Group related fields → extract to helper → orchestrate in main method

### 2. Pagination Loop Simplification
**When**: Do-while loop with pagination logic >10 lines
**How**: Extract page processing → use recursion or helper method → main becomes coordinator

### 3. Document Building Standardization
**When**: Any `*.builder()...build()` pattern
**How**: Always split into: basic fields, statistics, metadata

---

## Anti-Patterns Avoided

### ❌ Don't Trust Plan Assumptions
- Plan said "verification only" ≠ actually compliant
- Always verify yourself

### ❌ Don't Skip Final Verification
- Refactoring can introduce new violations
- AST grep must be run at the end

### ❌ Don't Batch Unrelated Changes
- Each commit = 1 atomic unit
- Easier to revert, easier to review

### ❌ Don't Modify Without Reading
- Read actual file contents
- Don't just trust line numbers from subagent

---

## Tools That Saved Time

### ast_grep_search
- **Purpose**: Find ternary operators, complex patterns
- **Reliability**: 100% accurate for structural patterns
- **Usage**: Always run in final verification

### Physical Line Counting
- **Method**: Manual count from `{` to `}`
- **Why**: Objective, no interpretation needed
- **When**: After every method extraction

### Project-Level Compilation
- **Command**: `./gradlew :common:compileJava :harvester:compileJava :backend:compileJava -x test`
- **Why**: Catches cross-module issues
- **When**: After every task completion

---

## Next Phase Preparation

### Phase 3 Candidates (NOT done in Phase 2)
- [ ] Utility class extraction for common patterns
- [ ] DRY refactoring (remove code duplication)
- [ ] GraphQL query optimization
- [ ] Common pagination helper

### Blockers Identified
- None - Phase 2 complete

### Technical Debt Created
- None - All code is KOSP compliant

---

**Phase 2 Status**: ✅ COMPLETE (8/8 tasks, all checkboxes marked)
