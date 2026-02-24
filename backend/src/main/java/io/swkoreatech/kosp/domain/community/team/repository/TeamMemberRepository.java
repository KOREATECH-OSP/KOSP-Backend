package io.swkoreatech.kosp.domain.community.team.repository;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;

import java.util.Optional;

import org.springframework.data.repository.Repository;

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
}
