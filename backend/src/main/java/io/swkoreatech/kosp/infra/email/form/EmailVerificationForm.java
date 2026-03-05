package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

/**
 * 이메일 인증 양식.
 *
 * <p>회원가입 시 이메일 인증 코드를 포함한 이메일 양식을 생성한다.
 */
public class EmailVerificationForm implements EmailForm {

    private static final String SUBJECT = "KOSP 이메일 인증";
    private static final String PATH = "email_verification";

    private final String verificationCode;

    /**
     * @param verificationCode 이메일 인증 코드
     */
    public EmailVerificationForm(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getContent() {
        return Map.of("code", verificationCode);
    }

    /** {@inheritDoc} */
    @Override
    public String getSubject() {
        return SUBJECT;
    }

    /** {@inheritDoc} */
    @Override
    public String getFilePath() {
        return PATH;
    }
}
