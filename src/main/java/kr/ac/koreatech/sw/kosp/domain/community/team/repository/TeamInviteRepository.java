package kr.ac.koreatech.sw.kosp.domain.community.team.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.TeamInvite;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public interface TeamInviteRepository extends Repository<TeamInvite, Long> {

    TeamInvite save(TeamInvite teamInvite);

    void delete(TeamInvite teamInvite);

    Optional<TeamInvite> findById(Long id);

    Optional<TeamInvite> findByTeamAndInvitee(Team team, User invitee);

    boolean existsByTeamAndInvitee(Team team, User invitee);
}
