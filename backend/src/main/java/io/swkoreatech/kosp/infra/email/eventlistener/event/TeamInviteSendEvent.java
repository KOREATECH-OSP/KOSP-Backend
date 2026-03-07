package io.swkoreatech.kosp.infra.email.eventlistener.event;

/**
 * 팀 초대 이메일 발송 이벤트.
 *
 * <p>팀에 새 멤버를 초대할 때 발행되며, 이메일 리스너에서 수신하여 초대 메일을 발송한다.
 *
 * @param email       초대받는 사용자의 이메일 주소
 * @param teamName    팀 이름
 * @param inviterName 초대한 사용자 이름
 * @param inviteId    초대 ID
 * @param clientUrl   클라이언트 기본 URL
 */
public record TeamInviteSendEvent(
    String email,
    String teamName,
    String inviterName,
    Long inviteId,
    String clientUrl
) {
}
