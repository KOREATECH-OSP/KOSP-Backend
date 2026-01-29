-- ============================================
-- Recruit canApply Test Data
-- ============================================
-- Test data for RecruitService.canApply() scenarios
-- 
-- Scenarios:
-- - User 100: Can apply (no existing application)
-- - User 101: Has PENDING application (cannot apply)
-- - User 102: Is team member (cannot apply)
-- - User 103: Has REJECTED application (can apply again)
-- ============================================

-- ============================================
-- 1. Users (IDs: 100-103)
-- ============================================

INSERT INTO users (
    id, name, kut_id, kut_email, password, introduction, point, is_deleted, created_at, updated_at
) VALUES
(100, 'Test User Can Apply', 'user-can-apply', 'user-can-apply@koreatech.ac.kr', 'encoded_password', 'Can apply user', 0, false, NOW(), NOW()),
(101, 'Test User Has Applied', 'user-has-applied', 'user-has-applied@koreatech.ac.kr', 'encoded_password', 'Has pending application', 0, false, NOW(), NOW()),
(102, 'Test User Is Member', 'user-is-member', 'user-is-member@koreatech.ac.kr', 'encoded_password', 'Is team member', 0, false, NOW(), NOW()),
(103, 'Test User Rejected Apply', 'user-rejected-apply', 'user-rejected-apply@koreatech.ac.kr', 'encoded_password', 'Has rejected application', 0, false, NOW(), NOW());

-- ============================================
-- 2. Board (required for Article/Recruit)
-- ============================================

INSERT INTO board (
    id, name, description, is_recruit_allowed, is_notice, created_at, updated_at
) VALUES
(1, 'Recruit Board', 'Board for recruitment posts', true, false, NOW(), NOW());

-- ============================================
-- 3. Team (ID: 50)
-- ============================================

INSERT INTO team (
    id, name, description, image_url, is_deleted, created_at, updated_at
) VALUES
(50, 'Test Team For Recruit', 'Team for testing recruit canApply', NULL, false, NOW(), NOW());

-- ============================================
-- 4. Article (base table for Recruit - JOINED inheritance)
-- ============================================

INSERT INTO article (
    id, board_id, author_id, title, content, views, likes, comments_count, is_deleted, is_pinned, dtype, created_at, updated_at
) VALUES
(200, 1, 100, 'Backend Developer', 'We are looking for a backend developer', 0, 0, 0, false, false, 'RECRUIT', NOW(), NOW()),
(201, 1, 100, 'Deleted Recruit', 'This recruit is deleted', 0, 0, 0, true, false, 'RECRUIT', NOW(), NOW());

-- ============================================
-- 5. Recruit (extends Article via JOINED inheritance)
-- ============================================

INSERT INTO recruit (
    id, team_id, status, start_date, end_date
) VALUES
(200, 50, 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(201, 50, 'CLOSED', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

-- ============================================
-- 6. RecruitApply (applications)
-- ============================================

INSERT INTO recruit_apply (
    id, recruit_id, user_id, status, reason, portfolio_url, decision_reason, created_at, updated_at
) VALUES
(1, 200, 101, 'PENDING', 'I am interested in this position', 'https://portfolio.example.com/user101', 'Pending review', NOW(), NOW()),
(2, 200, 103, 'REJECTED', 'I want to join this team', 'https://portfolio.example.com/user103', 'Not a good fit', NOW(), NOW());

-- ============================================
-- 7. TeamMember (team membership)
-- ============================================

INSERT INTO team_member (
    id, team_id, user_id, role, is_deleted, created_at, updated_at
) VALUES
(1, 50, 102, 'MEMBER', false, NOW(), NOW());
