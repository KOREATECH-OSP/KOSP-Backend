package kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event;

public record EmailVerificationSendEvent(
    String email,
    String verificationCode
) {
}
