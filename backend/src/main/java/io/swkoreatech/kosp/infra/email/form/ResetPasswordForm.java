package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

/**
 * 비밀번호 초기화 이메일 양식.
 *
 * <p>비밀번호 초기화 요청 시 리셋 토큰과 클라이언트 URL을 포함한 이메일 양식을 생성한다.
 */
public class ResetPasswordForm implements EmailForm {

    private static final String SUBJECT = "KOSP 비밀번호 초기화";
    private static final String PATH = "reset_password_button";

    private final String contextPath;
    private final String resetToken;

    /**
     * @param contextPath 클라이언트 기본 URL
     * @param resetToken  비밀번호 리셋 토큰
     */
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
