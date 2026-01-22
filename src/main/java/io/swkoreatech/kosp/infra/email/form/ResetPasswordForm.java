package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

public class ResetPasswordForm implements EmailForm {
    private static final String SUBJECT = "KOSP 비밀번호 초기화";
    private static final String PATH = "reset_password_button";

    private final String contextPath;
    private final String resetToken;

    public ResetPasswordForm(String contextPath, String resetToken) {
        this.contextPath = contextPath;
        this.resetToken = resetToken;
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of(
            "contextPath", contextPath,
            "resetToken", resetToken
        );
    }

    @Override
    public String getSubject() {
        return SUBJECT;
    }

    @Override
    public String getFilePath() {
        return PATH;
    }
}
