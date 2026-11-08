package io.swkoreatech.kosp.domain.community.team.model;

/**
 * 초대 제한을 집계하는 축.
 *
 * <p>두 축을 함께 적용해 우회 초대를 막는다. 초대 시 둘 중 하나라도
 * 차단 상태이면 초대를 거부한다.</p>
 */
public enum InviteRestrictionScope {

    /**
     * 팀 단위 제한.
     * A팀이 영희에게 3회 거절당하면 <b>A팀의 어떤 관리자도</b> 영희를 다시 초대할 수 없다.
     */
    TEAM,

    /**
     * 초대자 단위 제한.
     * 철수가 영희에게 3회 거절당하면 <b>철수가 관리하는 다른 팀으로도</b> 영희를 초대할 수 없다.
     */
    INVITER
}
