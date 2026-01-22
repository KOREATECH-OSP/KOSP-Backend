package io.swkoreatech.kosp.domain.community.team.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.user.model.User;

public interface TeamMemberRepository extends Repository<TeamMember, Long> {

    TeamMember save(TeamMember teamMember);
    void delete(TeamMember teamMember);
    boolean existsByTeamAndUser(Team team, User user);
    Optional<TeamMember> findByTeamAndUser(Team team, User user);
    Optional<TeamMember> findByUser(User user);
    java.util.List<TeamMember> findAllByUser(User user);
}
