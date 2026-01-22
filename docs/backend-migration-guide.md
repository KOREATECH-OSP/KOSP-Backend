# Backend Migration Guide for Harvester Integration

이 문서는 KOSP-Backend가 KOSP-Github-Harvester의 데이터를 활용하기 위해 필요한 변경사항을 기술합니다.

---

## 1. 공유 테이블 스키마 변경

### 1.1 github_user_statistics 테이블

**필드명 변경 필요:**

| 현재 (Backend) | 변경 후 | 용도 |
|----------------|---------|------|
| `main_repo_score` | `activity_score` | 활동 수준 점수 (0~3) |
| `other_repo_score` | `diversity_score` | 다양성 점수 (0~1) |
| `reputation_score` | `impact_score` | 영향력 점수 (0~5) |
| `pr_issue_score` | _(삭제)_ | 미사용 |

**SQL Migration:**
```sql
ALTER TABLE github_user_statistics 
  CHANGE COLUMN main_repo_score activity_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  CHANGE COLUMN other_repo_score diversity_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  CHANGE COLUMN reputation_score impact_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  DROP COLUMN pr_issue_score;
```

### 1.2 platform_statistics 테이블 (신규)

Harvester가 매시 정각에 전체 평균을 계산하여 저장합니다.

```sql
CREATE TABLE platform_statistics (
  stat_key VARCHAR(50) PRIMARY KEY,
  avg_commit_count DECIMAL(15,2) NOT NULL DEFAULT 0,
  avg_star_count DECIMAL(15,2) NOT NULL DEFAULT 0,
  avg_pr_count DECIMAL(15,2) NOT NULL DEFAULT 0,
  avg_issue_count DECIMAL(15,2) NOT NULL DEFAULT 0,
  total_user_count INT NOT NULL DEFAULT 0,
  calculated_at DATETIME NOT NULL
);
```

---

## 2. Entity 변경

### 2.1 GithubUserStatistics.java

```java
// 변경 전
private BigDecimal mainRepoScore;
private BigDecimal otherRepoScore;
private BigDecimal prIssueScore;
private BigDecimal reputationScore;

// 변경 후
private BigDecimal activityScore;    // 4.1 활동 수준 (0~3)
private BigDecimal diversityScore;   // 4.2 다양성 (0~1)
private BigDecimal impactScore;      // 4.3 영향력 (0~5)
// prIssueScore 제거

// updateDetailedScore() 삭제, updateScores() 사용
public void updateScores(
    BigDecimal activityScore,
    BigDecimal diversityScore,
    BigDecimal impactScore
) {
    this.activityScore = activityScore;
    this.diversityScore = diversityScore;
    this.impactScore = impactScore;
    this.totalScore = activityScore.add(diversityScore).add(impactScore);
}
```

### 2.2 PlatformStatistics.java (신규 Entity)

```java
@Entity
@Table(name = "platform_statistics")
public class PlatformStatistics {
    @Id
    private String statKey;  // "global"
    
    private BigDecimal avgCommitCount;
    private BigDecimal avgStarCount;
    private BigDecimal avgPrCount;
    private BigDecimal avgIssueCount;
    private Integer totalUserCount;
    private LocalDateTime calculatedAt;
}
```

---

## 3. API Response 매핑

### 3.1 overall-history (Section 2)

| API 필드 | DB 컬럼 (github_user_statistics) |
|----------|----------------------------------|
| `contributedRepoCount` | `contributed_repos_count` |
| `totalCommitCount` | `total_commits` |
| `totalAdditions` | `total_additions` |
| `totalDeletions` | `total_deletions` |
| `totalIssueCount` | `total_issues` |
| `totalPrCount` | `total_prs` |

### 3.2 contribution-comparison (Section 3)

| API 필드 | DB 테이블.컬럼 |
|----------|----------------|
| `avgCommitCount` | `platform_statistics.avg_commit_count` |
| `avgStarCount` | `platform_statistics.avg_star_count` |
| `avgPrCount` | `platform_statistics.avg_pr_count` |
| `avgIssueCount` | `platform_statistics.avg_issue_count` |
| `userCommitCount` | `github_user_statistics.total_commits` |
| `userStarCount` | `github_user_statistics.total_stars_received` |
| `userPrCount` | `github_user_statistics.total_prs` |
| `userIssueCount` | `github_user_statistics.total_issues` |

### 3.3 contribution-score (Section 4)

| API 필드 | DB 컬럼 (github_user_statistics) |
|----------|----------------------------------|
| `activityScore` | `activity_score` |
| `diversityScore` | `diversity_score` |
| `impactScore` | `impact_score` |
| `totalScore` | `total_score` |

---

## 4. MongoDB Collections (조회 전용)

Harvester가 수집한 상세 데이터는 MongoDB에 저장됩니다. 
Backend에서 상세 조회 API가 필요하면 아래 컬렉션을 조회하세요.

| Collection | 용도 | 주요 필드 |
|------------|------|----------|
| `github_contributed_repos` | 최근 기여활동 (Section 1) | repositoryName, description, stargazersCount, userCommitCount, userPrCount |
| `github_commits` | 커밋 상세 | sha, message, additions, deletions, authoredAt |
| `github_pull_requests` | PR 상세 | prNumber, title, state, merged, repoStarCount, closedIssuesCount, isCrossRepository |
| `github_issues` | 이슈 상세 | issueNumber, title, state, commentsCount |

---

## 5. 점수 계산 로직 (참고용)

### 5.1 Activity Score (0~3점)

레포별 commit/PR 수 중 최고점:
- 3점: commits ≥ 100 AND prs ≥ 20
- 2점: commits ≥ 30 AND prs ≥ 5
- 1점: commits ≥ 5 OR prs ≥ 1
- 0점: 미충족

### 5.2 Diversity Score (0~1점)

- 1.0: repos ≥ 10
- 0.7: repos ≥ 5
- 0.4: repos ≥ 2
- 0: repos ≤ 1

### 5.3 Impact Score (0~5점, 보너스 누적)

- +2.0: 소유 레포 stars ≥ 100
- +1.5: merged PR to repo with stars ≥ 1000
- +1.0: closed issues by PR ≥ 10
- +0.5: cross-repository PR merged (fork → upstream)

---

## 6. 적용 순서

1. DB 마이그레이션 실행 (테이블 변경 + 신규 테이블)
2. Backend Entity 수정
3. Backend Repository 추가 (PlatformStatisticsRepository)
4. API Response DTO 수정
5. 테스트 및 배포
