package io.swkoreatech.kosp.infra.email.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import io.swkoreatech.kosp.infra.email.eventlistener.event.EmailVerificationSendEvent;
import io.swkoreatech.kosp.infra.email.eventlistener.event.ResetPasswordEvent;
import io.swkoreatech.kosp.infra.email.eventlistener.event.TeamInviteSendEvent;
import io.swkoreatech.kosp.infra.email.form.EmailForm;
import io.swkoreatech.kosp.infra.email.form.EmailVerificationForm;
import io.swkoreatech.kosp.infra.email.form.ResetPasswordForm;
import io.swkoreatech.kosp.infra.email.form.TeamInviteForm;
import io.swkoreatech.kosp.infra.email.service.EmailService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile({"!test"})
public class EmailEventListener {

    private final EmailService emailService;

    @TransactionalEventListener
    public void onEmailVerificationSendEvent(EmailVerificationSendEvent event) {
        EmailForm emailForm = new EmailVerificationForm(event.verificationCode());
        emailService.sendEmail(event.email(), emailForm);
    }

    @TransactionalEventListener
    public void onResetPasswordSendEvent(ResetPasswordEvent event) {
        EmailForm emailForm = new ResetPasswordForm(event.clientUrl(), event.resetToken());
        emailService.sendEmail(event.email(), emailForm);
    }

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
