package io.swkoreatech.kosp.infra.email.eventlistener.event;

/**
 * TeamInviteSendEvent
 *
 * @param email       Invitee's email
 * @param teamName    Name of the team
 * @param inviterName Name of the inviter
 * @param inviteId    ID of the invitation
 */
public record TeamInviteSendEvent(
    String email,
    String teamName,
    String inviterName,
    Long inviteId,
    String clientUrl
) {
}
