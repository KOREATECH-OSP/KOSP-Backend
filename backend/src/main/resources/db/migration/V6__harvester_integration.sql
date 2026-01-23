-- V6: Harvester 통합을 위한 github_user_statistics 필드명 변경 및 platform_statistics 테이블 추가

-- 1. github_user_statistics 점수 필드명 변경
ALTER TABLE github_user_statistics 
  CHANGE COLUMN main_repo_score activity_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  CHANGE COLUMN other_repo_score diversity_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  CHANGE COLUMN reputation_score impact_score DECIMAL(10,2) NOT NULL DEFAULT 0,
  DROP COLUMN pr_issue_score;

-- 2. platform_statistics 테이블 생성 (전체 사용자 평균 통계)
CREATE TABLE platform_statistics (
  stat_key VARCHAR(50) PRIMARY KEY,
  avg_commit_count DECIMAL(15,2) NOT NULL DEFAULT 0,
  avg_star_count DECIMAL(15,2) NOT NULL DEFAULT 0,
  avg_pr_count DECIMAL(15,2) NOT NULL DEFAULT 0,
  avg_issue_count DECIMAL(15,2) NOT NULL DEFAULT 0,
  total_user_count INT NOT NULL DEFAULT 0,
  calculated_at DATETIME NOT NULL
);

-- 3. 초기 데이터 삽입
INSERT INTO platform_statistics (stat_key, avg_commit_count, avg_star_count, avg_pr_count, avg_issue_count, total_user_count, calculated_at)
VALUES ('global', 0, 0, 0, 0, 0, NOW());
