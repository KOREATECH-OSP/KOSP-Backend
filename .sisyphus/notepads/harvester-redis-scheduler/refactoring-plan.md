# Harvester Code Quality Refactoring Plan

## Phase 1: KOSP Coding Rules Compliance

### Task 1: Remove Ternary Operators
**Target**: ScoreCalculationStep.java (9 violations)
**Lines**: 173, 181, 187, 190, 198, etc.
**Pattern**:
```java
// BEFORE (VIOLATION)
return hasHighStarRepo ? BigDecimal.valueOf(2) : BigDecimal.ZERO;

// AFTER (COMPLIANT)
private BigDecimal calculateBonus(boolean condition, double value) {
    if (!condition) {
        return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(value);
}
```

### Task 2: Remove Stream Ternary Operators
**Target**: StatisticsAggregationStep.java (4 violations)
**Lines**: 103, 107, 120, 125
**Pattern**:
```java
// BEFORE (VIOLATION)
.mapToInt(c -> c.getAdditions() != null ? c.getAdditions() : 0)

// AFTER (COMPLIANT)
.mapToInt(this::getAdditionsOrZero)

private int getAdditionsOrZero(CommitDocument commit) {
    if (commit.getAdditions() == null) {
        return 0;
    }
    return commit.getAdditions();
}
```

### Task 3: Split Long Methods
**Targets**:
- StatisticsAggregationStep.aggregateFromMongoDB() (47 lines)
- ScoreCalculationStep.calculateActivityScore() (24 lines)
- ScoreCalculationStep.calculateDiversityScore() (15 lines)

## Phase 2: Extract Common Utilities (Later)

- StepContextUtils
- PaginationHelper
- StreamUtils
- GraphQLErrorHandler

