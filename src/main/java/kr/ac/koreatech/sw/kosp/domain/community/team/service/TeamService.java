package kr.ac.koreatech.sw.kosp.domain.community.team.service;

import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamDetailResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.TeamMember;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.TeamRole;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamMemberRepository;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public Long create(User user, TeamCreateRequest req) {
        Team team = Team.builder()
            .name(req.name())
            .description(req.description())
            .imageUrl(req.imageUrl())
            .build();
        teamRepository.save(team);

        TeamMember leader = TeamMember.builder()
            .team(team)
            .user(user)
            .role(TeamRole.LEADER)
            .build();
        teamMemberRepository.save(leader);

        return team.getId();
    }

    public TeamDetailResponse getTeam(Long teamId) {
        Team team = teamRepository.getById(teamId);
        return TeamDetailResponse.from(team);
    }

    public TeamListResponse getList(String search, Pageable pageable) {
        Page<Team> page = teamRepository.findByNameContaining(search, pageable);
        List<TeamResponse> teams = page.getContent().stream()
            .map(team -> TeamResponse.from(team, getLeaderName(team)))
            .toList();
        return new TeamListResponse(teams, PageMeta.from(page));
    }

    private String getLeaderName(Team team) {
        return team.getMembers().stream()
            .filter(member -> member.getRole() == TeamRole.LEADER)
            .findFirst()
            .map(member -> member.getUser().getName())
            .orElse("Unknown");
    }
}
