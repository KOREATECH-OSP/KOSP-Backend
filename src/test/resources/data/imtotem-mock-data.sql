-- ============================================
-- ImTotem 사용자 통계 Mock Data
-- ============================================
-- 
-- 전제조건:
-- - GitHub ID (숫자): 46699595
-- - GitHub Login (문자열): ImTotem
-- - User와 GithubUser는 이미 존재한다고 가정
-- 
-- 주의사항:
-- - 통계 테이블의 github_id 컬럼은 VARCHAR 타입으로 github_login 값('ImTotem')을 저장
-- - User 테이블의 github_id 컬럼은 BIGINT 타입으로 숫자 ID(46699595)를 저장
-- ============================================

-- ============================================
-- 3. GithubUserStatistics (전체 통계)
-- ============================================

INSERT INTO github_user_statistics (
    github_id,
    total_commits,
    total_lines,
    total_additions,
    total_deletions,
    total_prs,
    total_issues,
    owned_repos_count,
    contributed_repos_count,
    total_stars_received,
    total_forks_received,
    night_commits,
    day_commits,
    main_repo_score,
    other_repo_score,
    pr_issue_score,
    reputation_score,
    total_score,
    calculated_at,
    data_period_start,
    data_period_end
) VALUES (
    'ImTotem',
    850,        -- 총 커밋 수
    38000,      -- 총 라인 수
    24000,      -- 추가된 라인
    14000,      -- 삭제된 라인
    65,         -- PR 수
    32,         -- Issue 수
    8,          -- 소유 저장소 수
    18,         -- 기여 저장소 수
    420,        -- 받은 스타 수
    85,         -- 받은 포크 수
    280,        -- 밤 시간 커밋
    570,        -- 낮 시간 커밋
    680.00,     -- 메인 저장소 점수
    420.50,     -- 기타 저장소 점수
    185.00,     -- PR/Issue 점수
    180.00,     -- 평판 점수
    1465.50,    -- 총 점수
    NOW(),
    '2023-03-01',
    '2024-12-31'
);

-- ============================================
-- 4. GithubMonthlyStatistics (월별 활동)
-- ============================================

INSERT INTO github_monthly_statistics (
    github_id, `year`, `month`,
    commits_count, lines_count, additions_count, deletions_count,
    prs_count, issues_count, created_repos_count, contributed_repos_count,
    calculated_at
) VALUES
-- 2024년 데이터 (최근 12개월)
('ImTotem', 2024, 1, 68, 2800, 1800, 1000, 5, 2, 0, 2, NOW()),
('ImTotem', 2024, 2, 72, 3100, 2000, 1100, 6, 3, 1, 2, NOW()),
('ImTotem', 2024, 3, 85, 3600, 2300, 1300, 7, 4, 0, 3, NOW()),
('ImTotem', 2024, 4, 62, 2500, 1600, 900, 4, 2, 0, 1, NOW()),
('ImTotem', 2024, 5, 95, 4200, 2800, 1400, 8, 5, 2, 3, NOW()),
('ImTotem', 2024, 6, 58, 2200, 1400, 800, 3, 1, 0, 2, NOW()),
('ImTotem', 2024, 7, 102, 4500, 3000, 1500, 9, 6, 1, 4, NOW()),
('ImTotem', 2024, 8, 78, 3300, 2100, 1200, 6, 3, 0, 2, NOW()),
('ImTotem', 2024, 9, 88, 3800, 2500, 1300, 7, 4, 1, 3, NOW()),
('ImTotem', 2024, 10, 65, 2600, 1700, 900, 4, 2, 0, 1, NOW()),
('ImTotem', 2024, 11, 92, 3900, 2600, 1300, 8, 3, 1, 2, NOW()),
('ImTotem', 2024, 12, 85, 3500, 2200, 1300, 7, 2, 0, 3, NOW());

-- ============================================
-- 5. GithubContributionPattern (기여 패턴)
-- ============================================

INSERT INTO github_contribution_pattern (
    github_id,
    night_owl_score,
    night_commits,
    day_commits,
    initiator_score,
    early_contributions,
    independent_score,
    solo_projects,
    total_projects,
    total_coworkers,
    hourly_distribution,
    calculated_at
) VALUES (
    'ImTotem',
    33,     -- Night Owl 점수 (33%)
    280,    -- 밤 시간 커밋
    570,    -- 낮 시간 커밋
    58,     -- Initiator 점수
    10,     -- 초기 기여 프로젝트 수
    42,     -- Independent 점수
    4,      -- 혼자 작업한 프로젝트
    18,     -- 전체 프로젝트 수
    24,     -- 협업자 수
    '{"0":8,"1":6,"2":4,"3":2,"4":1,"5":3,"6":12,"7":25,"8":42,"9":58,"10":68,"11":72,"12":65,"13":62,"14":70,"15":78,"16":75,"17":65,"18":52,"19":38,"20":28,"21":20,"22":15,"23":10}',
    NOW()
);

-- ============================================
-- 6. GithubRepositoryStatistics (저장소별 통계)
-- ============================================

INSERT INTO github_repository_statistics (
    repo_owner, repo_name, contributor_github_id,
    stargazers_count, forks_count, watchers_count,
    total_commits_count, total_prs_count, total_issues_count,
    user_commits_count, user_prs_count, user_issues_count,
    last_commit_date, description, primary_language,
    calculated_at
) VALUES
-- ImTotem의 메인 프로젝트
('ImTotem', 'KOSP-Backend', 'ImTotem', 15, 5, 3, 850, 65, 32, 420, 35, 18, '2024-12-20 15:30:00', 'KOSP 프로젝트 백엔드 서버', 'Java', NOW()),
('ImTotem', 'algorithm-study', 'ImTotem', 8, 2, 2, 180, 0, 5, 180, 0, 5, '2024-11-15 10:20:00', '알고리즘 문제 풀이 저장소', 'Python', NOW()),
('ImTotem', 'spring-practice', 'ImTotem', 5, 1, 1, 120, 8, 3, 120, 8, 3, '2024-10-28 14:45:00', 'Spring Boot 학습 프로젝트', 'Java', NOW()),

-- 기여한 오픈소스 프로젝트
('KOREATECH-OSP', 'KOSP-Frontend', 'ImTotem', 12, 4, 3, 450, 28, 15, 85, 12, 4, '2024-12-18 11:00:00', 'KOSP 프로젝트 프론트엔드', 'TypeScript', NOW()),
('spring-projects', 'spring-boot', 'ImTotem', 68000, 28000, 15000, 125000, 15000, 8500, 15, 3, 1, '2024-09-10 09:30:00', 'Spring Boot framework', 'Java', NOW());

-- ============================================
-- 7. GithubYearlyStatistics (연도별 통계)
-- ============================================

INSERT INTO github_yearly_statistics (
    github_id, `year`,
    commits, `lines`, additions, deletions,
    prs, issues,
    total_score, main_repo_score, other_repo_score, pr_issue_score, reputation_score,
    `rank`, percentile,
    best_repo_owner, best_repo_name, best_repo_commits,
    calculated_at
) VALUES
-- 2024년
('ImTotem', 2024, 850, 38000, 24000, 14000, 65, 32, 1465.50, 680.00, 420.50, 185.00, 180.00, 20, 80, 'ImTotem', 'KOSP-Backend', 420, NOW()),

-- 2023년 (일부 데이터)
('ImTotem', 2023, 320, 14000, 9000, 5000, 22, 12, 580.00, 280.00, 180.00, 70.00, 50.00, 45, 55, 'ImTotem', 'algorithm-study', 180, NOW());

-- ============================================
-- 8. GithubLanguageStatistics (언어 분포)
-- ============================================

INSERT INTO github_language_statistics (
    github_id, language,
    lines_of_code, percentage, repositories, commits,
    calculated_at
) VALUES
-- ImTotem의 주요 사용 언어
('ImTotem', 'Java', 22000, 57.9, 5, 520, NOW()),
('ImTotem', 'Python', 8000, 21.1, 3, 180, NOW()),
('ImTotem', 'TypeScript', 5000, 13.2, 2, 95, NOW()),
('ImTotem', 'JavaScript', 2000, 5.3, 2, 42, NOW()),
('ImTotem', 'SQL', 1000, 2.6, 1, 13, NOW());
