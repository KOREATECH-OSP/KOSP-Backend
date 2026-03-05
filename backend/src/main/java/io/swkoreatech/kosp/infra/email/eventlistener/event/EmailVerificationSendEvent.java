package io.swkoreatech.kosp.infra.email.eventlistener.event;

/**
 * 이메일 인증 코드 발송 이벤트.
 *
 * <p>회원가입 시 이메일 인증이 필요할 때 발행되며, 이메일 리스너에서 수신하여 인증 메일을 발송한다.
 *
 * @param email            인증 대상 이메일 주소
 * @param verificationCode 이메일 인증 코드
 */
public record EmailVerificationSendEvent(
    String email,
    String verificationCode
) {
}
