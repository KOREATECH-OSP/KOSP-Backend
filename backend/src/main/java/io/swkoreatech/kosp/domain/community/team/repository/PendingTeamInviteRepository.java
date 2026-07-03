package io.swkoreatech.kosp.domain.community.team.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.community.team.model.PendingTeamInvite;
import io.swkoreatech.kosp.domain.community.team.model.Team;

/**
 * {@link PendingTeamInvite} 엔티티 데이터 접근 레포지토리.
 */
public interface PendingTeamInviteRepository extends Repository<PendingTeamInvite, Long> {

    PendingTeamInvite save(PendingTeamInvite pendingTeamInvite);

    /**
     * 팀-이메일 조합의 대기 초대를 조회한다 (재초대 시 재사용용, 삭제 여부 무관).
     */
    Optional<PendingTeamInvite> findByTeamAndEmail(Team team, String email);

    /**
     * 특정 이메일로 발송된, 아직 전환되지 않은(삭제되지 않은) 대기 초대 목록을 조회한다.
     * 회원가입 완료 시 실제 초대로 전환할 대상.
     */
    List<PendingTeamInvite> findAllByEmailAndIsDeletedFalse(String email);
}
