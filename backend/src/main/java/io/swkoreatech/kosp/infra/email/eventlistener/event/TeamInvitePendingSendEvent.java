package io.swkoreatech.kosp.infra.email.eventlistener.event;

/**
 * 미가입자 팀 초대(가입 유도) 이메일 발송 이벤트.
 *
 * <p>아직 K-OSP에 가입하지 않은 코리아텍 이메일 사용자를 팀에 초대할 때 발행되며,
 * 이메일 리스너에서 수신하여 회원가입을 유도하는 초대 메일을 발송한다.</p>
 *
 * @param email       초대받는 (미가입) 이메일 주소
 * @param teamName    팀 이름
 * @param inviterName 초대한 사용자 이름
 * @param clientUrl   클라이언트 기본 URL (회원가입 링크 생성용)
 */
public record TeamInvitePendingSendEvent(
    String email,
    String teamName,
    String inviterName,
    String clientUrl
) {
}
