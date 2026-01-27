# Learnings - DRY Refactoring Phase 3

## Conventions & Patterns

*Accumulated knowledge from task execution will be logged here.*

---

## Task 1: StepContextHelper Utility (COMPLETED)

**Pattern Extracted**: 
- `getExecutionContext()` and `extractUserId()` methods are duplicated across 6 Step classes
- Both follow identical chain: `chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()`
- JobParameters accessed via: `chunkContext.getStepContext().getStepExecution().getJobParameters().getLong("userId")`

**Implementation**:
- Created `StepContextHelper.java` in `harvester/src/main/java/io/swkoreatech/kosp/collection/util/`
- 4 static methods: `getExecutionContext()`, `extractUserId()`, `extractString()`, `putString()`
- Private constructor with AssertionError to prevent instantiation
- All methods ≤ 6 lines (KOSP compliance)
- Explicit imports (no wildcards)
- Javadoc for all public methods (required per task spec)

**Verification**:
- Compilation: `./gradlew :harvester:compileJava -x test` → SUCCESS
- Commit: `feat(harvester): add StepContextHelper utility`

**Next Steps**: Tasks 5-11 will refactor 7 Step files to use this utility, eliminating 6 duplicate method definitions.

## Task 2: NullSafeGetters Utility Creation

**Completed**: 2026-01-27

### Implementation Details
- Created `NullSafeGetters.java` in `harvester/src/main/java/io/swkoreatech/kosp/collection/util/`
- Implemented two static methods:
  - `intOrZero(Integer value)` → returns 0 if null, else value
  - `longOrZero(Long value)` → returns 0L if null, else value
- Used early-return pattern (no ternary operators per KOSP rules)
- Private constructor with AssertionError for utility class pattern
- Javadoc for all public methods and class

### Pattern Reference
- Extracted pattern from StatisticsAggregationStep.java (4 methods) and ScoreCalculationStep.java (1 method)
- Each method follows 4-line pattern: if-check, return 0, return value
- Compilation: `./gradlew :harvester:compileJava -x test` → SUCCESS

### Commit
- Message: `feat(harvester): add NullSafeGetters utility`
- Hash: 16aef31

### Next Steps
- Task 3: Refactor StatisticsAggregationStep to use NullSafeGetters
- Task 4: Refactor ScoreCalculationStep to use NullSafeGetters

## GraphQLErrorHandler Utility Creation

**Pattern Extracted**: Consolidated 4 identical `logErrors()` methods from:
- CommitMiningStep.java:193-199
- PullRequestMiningStep.java:120-126
- IssueMiningStep.java:120-126
- RepositoryDiscoveryStep.java:122-128

**Implementation Details**:
- Static utility method: `logAndCheckErrors(GraphQLResponse<?>, String, String) → boolean`
- Returns true if errors exist (null response or hasErrors())
- Uses SLF4J logging with Lombok @Slf4j
- Private constructor with AssertionError for utility class pattern
- Javadoc required for public API documentation

**Key Insight**: GraphQLResponse has built-in `hasErrors()` method that checks both null and empty list conditions, simplifying the logic.

**LOC Reduction**: 28 lines (4 files × 7 lines) → 1 utility method (8 lines) = 20 LOC saved

**Next Steps**: Replace logErrors() calls in 4 Step files with GraphQLErrorHandler.logAndCheckErrors()

## GraphQLTypeFactory Creation (Task 4)

**Pattern Identified**: Unchecked cast for generic type erasure
- All 4 Step classes (Commit, PR, Issue, RepositoryDiscovery) had identical `createResponseType()` methods
- Java generics type erasure requires casting `GraphQLResponse.class` to `Class<GraphQLResponse<T>>`
- `@SuppressWarnings("unchecked")` is necessary and safe here

**Implementation Details**:
- Package: `io.swkoreatech.kosp.collection.util`
- Correct import: `io.swkoreatech.kosp.client.dto.GraphQLResponse` (not `harvester.client.graphql`)
- Utility class pattern: private constructor with `throw new AssertionError("Utility class")`
- Method: `public static <T> Class<GraphQLResponse<T>> responseType()`

**Verification**:
- ✅ Compilation: `./gradlew :harvester:compileJava -x test` SUCCESS
- ✅ Commit: `feat(harvester): add GraphQLTypeFactory utility`
- ✅ File: 30 lines (1 line method body, follows KOSP rules)

**Next Steps**: Replace 4 identical methods in Step classes with calls to `GraphQLTypeFactory.responseType()`

## Task 5: CommitMiningStep Refactoring (COMPLETED)

**Completed**: 2026-01-27

### Refactoring Summary
- Replaced 4 helper methods with utility class calls
- LOC reduction: 260 → 234 lines (26 lines saved, exceeds 20 line target)
- All 4 utilities successfully integrated

### Changes Made

1. **Added imports**:
   - `io.swkoreatech.kosp.collection.util.StepContextHelper`
   - `io.swkoreatech.kosp.collection.util.GraphQLErrorHandler`
   - `io.swkoreatech.kosp.collection.util.GraphQLTypeFactory`

2. **Replaced method calls**:
   - Line 60: `getExecutionContext(chunkContext)` → `StepContextHelper.getExecutionContext(chunkContext)`
   - Line 61: `extractUserId(chunkContext)` → `StepContextHelper.extractUserId(chunkContext)`
   - Line 173: `createResponseType()` → `GraphQLTypeFactory.<RepositoryCommitsResponse>responseType()`
   - Line 140: `logErrors(response, owner, name)` → `GraphQLErrorHandler.logAndCheckErrors(response, "repo", owner + "/" + name)`

3. **Deleted methods**:
   - `getExecutionContext()` (5 lines)
   - `extractUserId()` (5 lines)
   - `createResponseType()` (4 lines)
   - `logErrors()` (7 lines)
   - Total: 21 lines deleted

### Key Implementation Detail
- **Generic type parameter**: Must explicitly specify `<RepositoryCommitsResponse>` when calling `GraphQLTypeFactory.responseType()` to avoid type erasure issues
- Pattern: `GraphQLTypeFactory.<RepositoryCommitsResponse>responseType()`

### Verification
- ✅ Compilation: `./gradlew :harvester:compileJava -x test` SUCCESS
- ✅ LOC: 260 → 234 (26 line reduction)
- ✅ Commit: `refactor(harvester): use utilities in CommitMiningStep` (hash: 2ce12f1)

### Next Steps
- Tasks 6-11: Refactor remaining 6 Step files (PullRequestMiningStep, IssueMiningStep, RepositoryDiscoveryStep, StatisticsAggregationStep, ScoreCalculationStep, CleanupStep)
