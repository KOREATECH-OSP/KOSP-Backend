package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

public class EmailVerificationForm implements EmailForm {

    private static final String SUBJECT = "KOSP 이메일 인증";
    private static final String PATH = "email_verification";

    private final String verificationCode;

    public EmailVerificationForm(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of("code", verificationCode);
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
