package io.swkoreatech.kosp.infra.email.eventlistener.event;

public record ResetPasswordEvent(
    String email,
    String clientUrl,
    String resetToken
) {
}
