package io.swkoreatech.kosp.domain.community.team.eventlistener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.swkoreatech.kosp.domain.community.team.service.TeamService;
import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원가입 완료 시, 해당 이메일로 발송된 대기 팀 초대를 실제 팀 초대로 전환하는 리스너.
 *
 * <p>회원가입 트랜잭션 커밋 후 비동기로 처리한다. 연결 실패가 회원가입 자체에는
 * 영향을 주지 않도록 예외를 흡수하고 로깅한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingTeamInviteLinkListener {

    private final TeamService teamService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserSignup(UserSignupEvent event) {
        try {
            teamService.linkPendingInvitesForNewUser(event.getUserId());
        } catch (Exception e) {
            log.error("[PendingInvite] 회원가입 후 대기 초대 연결 실패. userId={}", event.getUserId(), e);
        }
    }
}
