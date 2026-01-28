## [2026-01-27] Known Issues

### Pre-existing Test Compilation Errors
**Status**: OUT OF SCOPE for this refactoring

**Description**:
- Multiple test files fail to compile
- Errors include:
  - Wrong package imports (kr.ac.koreatech.sw.kosp vs io.swkoreatech.kosp)
  - Missing classes (GithubCommitDetailRaw, various calculators)
  - Method signature changes (CommentRepository.delete, UserService.getMyApplications)

**Impact**:
- Cannot run `./gradlew build` without `-x test` flag
- Main source code compiles successfully
- Production code is unaffected

**Workaround**:
- Use `./gradlew build -x test` for builds
- Tests need separate refactoring effort

**Files Affected**:
- MonthlyStatisticsCalculatorTest.java
- UserStatisticsCalculatorTest.java
- YearlyStatisticsCalculatorTest.java
- ContributionPatternCalculatorTest.java
- GithubServiceTest.java
- GithubScoreCalculatorTest.java
- GithubStatisticsServiceTest.java
- AdminContentServiceTest.java
- PolicyAdminServiceTest.java
- UserServiceTest.java
- CommentServiceTest.java

**Recommendation**:
- Create separate task to fix test compilation
- Update package imports
- Fix method signatures
- Verify test coverage after fixes

