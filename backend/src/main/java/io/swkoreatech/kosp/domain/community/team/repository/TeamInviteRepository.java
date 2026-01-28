package io.swkoreatech.kosp.domain.community.team.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;
import io.swkoreatech.kosp.domain.user.model.User;

public interface TeamInviteRepository extends Repository<TeamInvite, Long> {

    TeamInvite save(TeamInvite teamInvite);

    void delete(TeamInvite teamInvite);

    Optional<TeamInvite> findById(Long id);

    Optional<TeamInvite> findByIdAndIsDeletedFalse(Long id);

    Optional<TeamInvite> findByTeamAndInvitee(Team team, User invitee);

    Optional<TeamInvite> findByTeamAndInviteeAndIsDeletedFalse(Team team, User invitee);

    boolean existsByTeamAndInvitee(Team team, User invitee);

    boolean existsByTeamAndInviteeAndIsDeletedFalse(Team team, User invitee);

    java.util.List<TeamInvite> findAllByTeam(Team team);
}
