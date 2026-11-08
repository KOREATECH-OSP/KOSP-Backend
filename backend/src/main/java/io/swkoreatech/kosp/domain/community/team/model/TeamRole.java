package io.swkoreatech.kosp.domain.community.team.model;

/**
 * 팀 역할.
 * LEADER: 팀장 (모든 권한), ADMIN: 관리자 (초대 권한), MEMBER: 일반 팀원.
 */
public enum TeamRole {

    LEADER, ADMIN, MEMBER
}
