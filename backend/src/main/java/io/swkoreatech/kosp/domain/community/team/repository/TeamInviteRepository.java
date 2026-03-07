package io.swkoreatech.kosp.domain.community.team.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;

/**
 * 팀 초대 리포지토리.
 * 팀 초대의 저장, 삭제 및 조회 기능을 제공한다.
 */
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

    default TeamInvite getById(Long id) {
        return findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
