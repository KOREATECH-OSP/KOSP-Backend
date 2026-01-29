-- Test data for repository search integration tests
-- Only includes github_repository_statistics table

INSERT INTO github_repository_statistics (
    repo_owner, repo_name, contributor_github_id,
    stargazers_count, forks_count, watchers_count,
    total_commits_count, total_prs_count, total_issues_count,
    user_commits_count, user_prs_count, user_issues_count,
    last_commit_date, description, primary_language,
    calculated_at
) VALUES
-- Java repositories with high stars
('spring-projects', 'spring-boot', 'testuser', 68000, 28000, 15000, 125000, 15000, 8500, 15, 3, 1, '2024-12-20 15:30:00', 'Spring Boot framework', 'Java', NOW()),
('ImTotem', 'KOSP-Backend', 'testuser', 150, 50, 30, 850, 65, 32, 420, 35, 18, '2024-12-20 15:30:00', 'KOSP 프로젝트 백엔드 서버', 'Java', NOW()),
('testorg', 'spring-practice', 'testuser', 50, 10, 5, 120, 8, 3, 120, 8, 3, '2024-10-28 14:45:00', 'Spring Boot 학습 프로젝트', 'Java', NOW()),

-- Python repositories
('testuser', 'algorithm-study', 'testuser', 80, 20, 15, 180, 0, 5, 180, 0, 5, '2024-11-15 10:20:00', '알고리즘 문제 풀이 저장소', 'Python', NOW()),
('pythonorg', 'django', 'testuser', 75000, 30000, 18000, 95000, 12000, 7000, 8, 2, 0, '2024-12-18 11:00:00', 'Django web framework', 'Python', NOW()),

-- TypeScript repository
('KOREATECH-OSP', 'KOSP-Frontend', 'testuser', 120, 40, 30, 450, 28, 15, 85, 12, 4, '2024-12-18 11:00:00', 'KOSP 프로젝트 프론트엔드', 'TypeScript', NOW()),

-- Low stars repository
('testuser', 'small-project', 'testuser', 5, 1, 1, 25, 2, 1, 25, 2, 1, '2024-09-10 09:30:00', 'Small test project', 'JavaScript', NOW());
