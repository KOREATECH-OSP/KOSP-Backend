package kr.ac.koreatech.sw.kosp.domain.community.team.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.TeamInvite;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, Long> {
    
    Optional<TeamInvite> findByTeamAndInvitee(Team team, User invitee);
    
    boolean existsByTeamAndInvitee(Team team, User invitee);

    // Fetch join might be needed later, but simple findById is sufficient for accept/reject logic usually
    // validation requires checking user matching, which is done in service
}
