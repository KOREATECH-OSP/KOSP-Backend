package io.swkoreatech.kosp.infra.email.eventlistener.event;

/**
 * 비밀번호 초기화 이메일 발송 이벤트.
 *
 * <p>사용자가 비밀번호 초기화를 요청할 때 발행되며, 이메일 리스너에서 수신하여 리셋 메일을 발송한다.
 *
 * @param email      비밀번호 초기화 대상 이메일 주소
 * @param clientUrl  클라이언트 기본 URL
 * @param resetToken 비밀번호 리셋 토큰
 */
public record ResetPasswordEvent(
    String email,
    String clientUrl,
    String resetToken
) {
}
