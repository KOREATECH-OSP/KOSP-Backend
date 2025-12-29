package kr.ac.koreatech.sw.kosp.infra.email.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event.EmailVerificationSendEvent;
import kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event.ResetPasswordEvent;
import kr.ac.koreatech.sw.kosp.infra.email.form.EmailForm;
import kr.ac.koreatech.sw.kosp.infra.email.form.EmailVerificationForm;
import kr.ac.koreatech.sw.kosp.infra.email.form.ResetPasswordForm;
import kr.ac.koreatech.sw.kosp.infra.email.service.EmailService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile({"!test"})
public class EmailEventListener {

    private final EmailService emailService;

    @TransactionalEventListener
    public void onEmailVerificationSendEvent(EmailVerificationSendEvent event) {
        EmailForm emailForm = new EmailVerificationForm(event.verificationCode());
        emailService.sendVerificationEmail(event.email(), emailForm);
    }

    @TransactionalEventListener
    public void onResetPasswordSendEvent(ResetPasswordEvent event) {
        EmailForm emailForm = new ResetPasswordForm(event.serverUrl(), event.resetToken());
        emailService.sendVerificationEmail(event.email(), emailForm);
    }
}
