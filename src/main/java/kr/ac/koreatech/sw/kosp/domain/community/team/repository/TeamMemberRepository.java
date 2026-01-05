package kr.ac.koreatech.sw.kosp.domain.community.team.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.TeamMember;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public interface TeamMemberRepository extends Repository<TeamMember, Long> {

    TeamMember save(TeamMember teamMember);
    void delete(TeamMember teamMember);
    boolean existsByTeamAndUser(Team team, User user);
    Optional<TeamMember> findByTeamAndUser(Team team, User user);
    Optional<TeamMember> findByUser(User user);
}
