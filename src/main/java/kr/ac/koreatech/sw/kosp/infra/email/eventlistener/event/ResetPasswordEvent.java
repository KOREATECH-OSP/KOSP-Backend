package kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event;

public record ResetPasswordEvent(
    String email,
    String serverUrl,
    String resetToken
) {
}
