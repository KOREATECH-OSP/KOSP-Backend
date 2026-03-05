package io.swkoreatech.kosp.infra.email.eventlistener;

import io.swkoreatech.kosp.infra.email.eventlistener.event.EmailVerificationSendEvent;
import io.swkoreatech.kosp.infra.email.eventlistener.event.ResetPasswordEvent;
import io.swkoreatech.kosp.infra.email.eventlistener.event.TeamInviteSendEvent;
import io.swkoreatech.kosp.infra.email.form.EmailForm;
import io.swkoreatech.kosp.infra.email.form.EmailVerificationForm;
import io.swkoreatech.kosp.infra.email.form.ResetPasswordForm;
import io.swkoreatech.kosp.infra.email.form.TeamInviteForm;
import io.swkoreatech.kosp.infra.email.service.EmailService;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

/**
 * 이메일 발송 이벤트 리스너.
 *
 * <p>트랜잭션 커밋 후 이메일 관련 이벤트를 수신하여 해당 이메일을 발송한다.
 * 테스트 프로파일에서는 비활성화된다.
 */
@Component
@RequiredArgsConstructor
@Profile({"!test"})
public class EmailEventListener {

    private final EmailService emailService;

    /**
     * 이메일 인증 발송 이벤트를 처리한다.
     *
     * @param event 이메일 인증 발송 이벤트
     */
    @TransactionalEventListener
    public void onEmailVerificationSendEvent(EmailVerificationSendEvent event) {
        EmailForm emailForm = new EmailVerificationForm(event.verificationCode());
        emailService.sendEmail(event.email(), emailForm);
    }

    /**
     * 비밀번호 초기화 이메일 발송 이벤트를 처리한다.
     *
     * @param event 비밀번호 초기화 이벤트
     */
    @TransactionalEventListener
    public void onResetPasswordSendEvent(ResetPasswordEvent event) {
        EmailForm emailForm = new ResetPasswordForm(event.clientUrl(), event.resetToken());
        emailService.sendEmail(event.email(), emailForm);
    }

    /**
     * 팀 초대 이메일 발송 이벤트를 처리한다.
     *
     * @param event 팀 초대 발송 이벤트
     */
    @TransactionalEventListener
    public void onTeamInviteSendEvent(TeamInviteSendEvent event) {
        EmailForm emailForm = new TeamInviteForm(
            event.teamName(),
            event.inviterName(),
            event.clientUrl(),
            event.inviteId()
        );
        emailService.sendEmail(event.email(), emailForm);
    }
}
