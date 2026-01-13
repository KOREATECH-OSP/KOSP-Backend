# GitHub Statistics Mock Data

이 디렉토리는 GitHub 통계 시스템 테스트를 위한 목업 데이터를 포함합니다.

## 📁 파일 구조

- `github-statistics-mock-data.sql` - 모든 통계 테이블에 대한 SQL INSERT 스크립트

## 📊 포함된 데이터

### 1. 테스트 사용자 (8명)

| GitHub ID | 프로필 | 특징 |
|-----------|--------|------|
| `octocat` | 균형잡힌 기여자 | 전반적으로 활발한 활동, 다양한 프로젝트 참여 |
| `torvalds` | 백엔드 전문가 | 높은 커밋 수, Linux 커널 메인테이너 |
| `gaearon` | 프론트엔드 개발자 | React 핵심 기여자, JavaScript/TypeScript 중심 |
| `sindresorhus` | 오픈소스 메인테이너 | 다수의 인기 프로젝트 소유, 높은 스타 수 |
| `newbie123` | 주니어 개발자 | 적은 활동량, 최근 시작 |
| `docwriter` | 문서화 전문가 | 문서 작성 중심, 낮은 코드 라인 수 |
| `nightcoder` | 야행성 개발자 | 밤 시간대 커밋 비율 80% |
| `fullstacker` | 풀스택 개발자 | 다양한 언어 사용, 협업 중심 |
| **`ImTotem`** | **KOSP 백엔드 개발자** | **실제 User 연동, Java 57.9%, 850 commits, GitHub ID: 46699595** |

### 2. 통계 테이블

#### `github_user_statistics` (8 records)
- 사용자별 전체 통계 (커밋, 라인, PR, 이슈 등)
- 점수 세분화 (main_repo_score, other_repo_score, pr_issue_score, reputation_score)
- 시간대 분석 (night_commits, day_commits)

#### `github_monthly_statistics` (30 records)
- `octocat`: 2024년 전체 12개월 데이터
- `torvalds`, `gaearon`: 최근 6개월 데이터
- 월별 활동 추이 분석 가능

#### `github_contribution_pattern` (4 records)
- 시간대 패턴 (hourly_distribution JSON)
- 프로젝트 패턴 (initiator_score, solo_projects)
- 협업 패턴 (total_coworkers)

#### `github_repository_statistics` (10 records)
- 주요 오픈소스 프로젝트 (linux, react, vscode, node 등)
- 저장소별 사용자 기여도
- 스타, 포크, 워처 수

#### `github_yearly_statistics` (6 records)
- 연도별 집계 데이터
- 2023-2024년 데이터

#### `github_language_statistics` (18 records)
- 사용자별 언어 분포
- JavaScript, TypeScript, Python, C 등

#### `github_score_config` (10 records)
- 점수 계산 설정값
- 가중치 및 보너스 설정

## 🚀 사용 방법

### 방법 1: SQL 스크립트 직접 실행

```bash
# MySQL/MariaDB
mysql -u username -p database_name < github-statistics-mock-data.sql

# H2 Database (테스트 환경)
# application-test.yml에서 H2 콘솔 활성화 후 SQL 복사/붙여넣기
```

### 방법 2: Spring Boot 테스트에서 사용

```java
@Sql(scripts = "/data/github-statistics-mock-data.sql")
@SpringBootTest
class GithubStatisticsTest {
    // 테스트 코드
}
```

### 방법 3: Flyway/Liquibase 마이그레이션

```
src/test/resources/db/migration/V999__insert_mock_data.sql
```

## 📈 검증 쿼리

```sql
-- 각 테이블의 레코드 수 확인
SELECT 'github_user_statistics' as table_name, COUNT(*) as count FROM github_user_statistics
UNION ALL
SELECT 'github_monthly_statistics', COUNT(*) FROM github_monthly_statistics
UNION ALL
SELECT 'github_contribution_pattern', COUNT(*) FROM github_contribution_pattern
UNION ALL
SELECT 'github_repository_statistics', COUNT(*) FROM github_repository_statistics
UNION ALL
SELECT 'github_yearly_statistics', COUNT(*) FROM github_yearly_statistics
UNION ALL
SELECT 'github_language_statistics', COUNT(*) FROM github_language_statistics
UNION ALL
SELECT 'github_score_config', COUNT(*) FROM github_score_config;

-- 사용자별 통계 확인
SELECT github_id, total_commits, total_score, calculated_at
FROM github_user_statistics
ORDER BY total_score DESC;

-- 월별 활동 추이
SELECT github_id, `year`, `month`, commits_count
FROM github_monthly_statistics
WHERE github_id = 'octocat'
ORDER BY `year`, `month`;
```

## 🎯 테스트 시나리오

### 1. 대시보드 API 테스트
- `octocat` 사용자로 전체 통계 조회
- 월별 활동 그래프 렌더링 검증

### 2. 행동 분석 테스트
- `nightcoder`의 야행성 패턴 검증
- `torvalds`의 아침형 패턴 검증

### 3. 저장소 통계 테스트
- `linux` 저장소의 대규모 기여도 검증
- `react` 저장소의 협업 패턴 검증

### 4. 점수 계산 테스트
- `sindresorhus`의 높은 reputation_score 검증
- `newbie123`의 낮은 전체 점수 검증

## 🔄 데이터 업데이트

새로운 테스트 케이스가 필요한 경우:

1. SQL 스크립트에 INSERT 문 추가
2. 기존 데이터와 일관성 유지 (github_id 참조)
3. calculated_at은 NOW() 사용
4. 검증 쿼리로 데이터 무결성 확인

## ⚠️ 주의사항

- 이 데이터는 **테스트 전용**입니다
- 프로덕션 환경에 절대 사용하지 마세요
- GitHub ID는 실제 사용자명을 사용하지만 데이터는 모두 가상입니다
- 외래 키 제약조건이 있는 경우 순서에 주의하세요
