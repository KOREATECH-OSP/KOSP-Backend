package io.swkoreatech.kosp.domain.community.team.service;

import org.springframework.stereotype.Service;

import io.swkoreatech.kosp.domain.community.team.dto.response.TeamInviteResponse;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;
import io.swkoreatech.kosp.domain.community.team.repository.TeamInviteRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamInviteService {

    private final TeamInviteRepository teamInviteRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamInviteResponse getInvite(Long inviteId) {
        TeamInvite invite = teamInviteRepository.getById(inviteId);
        int memberCount = countActiveMembers(invite);
        return TeamInviteResponse.from(invite, memberCount);
    }

    private int countActiveMembers(TeamInvite invite) {
        return (int) teamMemberRepository.findAllByTeam(invite.getTeam())
            .stream()
            .filter(member -> !member.isDeleted())
            .count();
    }
}
