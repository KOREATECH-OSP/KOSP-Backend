package io.swkoreatech.kosp.domain.community.team.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;

/**
 * 팀 멤버 리포지토리.
 * 팀 멤버의 저장, 삭제 및 조건 조회 기능을 제공한다.
 */
public interface TeamMemberRepository extends Repository<TeamMember, Long> {

    TeamMember save(TeamMember teamMember);

    void delete(TeamMember teamMember);

    boolean existsByTeamAndUser(Team team, User user);

    boolean existsByTeamAndUserAndIsDeletedFalse(Team team, User user);

    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    Optional<TeamMember> findByTeamAndUserAndIsDeletedFalse(Team team, User user);

    Optional<TeamMember> findByUser(User user);

    Optional<TeamMember> findByUserAndIsDeletedFalse(User user);

    java.util.List<TeamMember> findAllByUser(User user);

    java.util.List<TeamMember> findAllByUserAndIsDeletedFalse(User user);

    java.util.List<TeamMember> findAllByTeam(Team team);

    /**
     * 유저의 삭제되지 않은 팀 참여 수를 조회한다.
     * 칭호 평가 배치에서 TEAM_JOIN_COUNT_GTE 조건 확인에 사용한다.
     * TODO(2차): TeamJoinEvent 기반 실시간 칭호 지급으로 고도화 예정
     *
     * @param user 유저
     * @return 팀 참여 수
     */
    long countByUserAndIsDeletedFalse(User user);
}
