-- 팀 초대 반복 거절 제한.
--
-- 기존에는 team_invite(team_id, invitee_id) 행의 rejection_count 만으로 판정했다.
-- 이 구조는 "같은 팀의 다른 관리자" 우회는 막지만(같은 행을 공유),
-- "같은 초대자가 자신이 관리하는 다른 팀으로 우회"하는 경우를 막지 못한다.
--
-- 그래서 제한을 초대 행에서 분리하여 두 축으로 집계한다.
--   scope='TEAM'    → scope_id = team_id          (정책 A: 팀 단위 제한)
--   scope='INVITER' → scope_id = inviter_user_id  (정책 B: 초대자 단위 제한)
--
-- 정책: 영구 3회 누적 + rolling 24시간 cooldown.
--   거절 3회에 도달하면 blocked_until = 마지막 거절 시각 + 24h 로 설정한다.
--   cooldown 이 끝나도 rejection_count 는 초기화하지 않으므로, 이후 거절 1회마다
--   다시 24시간 차단된다(무한 반복 초대 방지).
CREATE TABLE invite_restriction
(
    id               BIGSERIAL PRIMARY KEY,
    scope            VARCHAR(10) NOT NULL,           -- 'TEAM' | 'INVITER'
    scope_id         BIGINT      NOT NULL,           -- team_id 또는 inviter user_id
    invitee_id       BIGINT      NOT NULL REFERENCES users (id),
    rejection_count  INT         NOT NULL DEFAULT 0,
    last_rejected_at TIMESTAMP,
    blocked_until    TIMESTAMP,                      -- 제한 종료 시각 (NULL 이면 제한 없음)
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_invite_restriction UNIQUE (scope, scope_id, invitee_id),
    CONSTRAINT ck_invite_restriction_scope CHECK (scope IN ('TEAM', 'INVITER'))
);

CREATE INDEX idx_invite_restriction_lookup
    ON invite_restriction (invitee_id, scope, scope_id);

-- 기존 team_invite 의 누적 거절 횟수를 TEAM 스코프로 이관한다.
-- (INVITER 스코프는 과거 초대자 이력을 신뢰할 수 없어 0 에서 시작한다.)
INSERT INTO invite_restriction (scope, scope_id, invitee_id, rejection_count, last_rejected_at, blocked_until)
SELECT 'TEAM',
       ti.team_id,
       ti.invitee_id,
       ti.rejection_count,
       ti.last_invited_at,
       CASE WHEN ti.rejection_count >= 3 THEN ti.last_invited_at + INTERVAL '24 hours' END
FROM team_invite ti
WHERE ti.rejection_count > 0
ON CONFLICT (scope, scope_id, invitee_id) DO NOTHING;
