package io.swkoreatech.kosp.infra.email.eventlistener.event;

public record EmailVerificationSendEvent(
    String email,
    String verificationCode
) {
}
