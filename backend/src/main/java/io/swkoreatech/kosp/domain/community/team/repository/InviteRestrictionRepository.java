package io.swkoreatech.kosp.domain.community.team.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.community.team.model.InviteRestriction;
import io.swkoreatech.kosp.domain.community.team.model.InviteRestrictionScope;

/**
 * 초대 제한 리포지토리.
 */
public interface InviteRestrictionRepository extends Repository<InviteRestriction, Long> {

    InviteRestriction save(InviteRestriction restriction);

    Optional<InviteRestriction> findByScopeAndScopeIdAndInviteeId(
        InviteRestrictionScope scope, Long scopeId, Long inviteeId);

    /** 특정 피초대자에 대해 걸려 있는 제한을 모두 조회한다 (진단·조회 API 용). */
    List<InviteRestriction> findAllByInviteeId(Long inviteeId);
}
