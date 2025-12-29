package kr.ac.koreatech.sw.kosp.infra.email.model;

import jakarta.validation.constraints.Email;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

public record EmailAddress(
    @Email(message = "이메일 형식을 지켜주세요.", regexp = EmailAddress.EMAIL_PATTERN)
    String email
) {
    private static final String LOCAL_PARTS_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@";
    private static final String DOMAIN_PATTERN = "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
    private static final String EMAIL_PATTERN = LOCAL_PARTS_PATTERN + DOMAIN_PATTERN;

    private static final String DOMAIN_SEPARATOR = "@";
    private static final String KOREATECH_DOMAIN = "koreatech.ac.kr";

    public static EmailAddress from(String email) {
        return new EmailAddress(email);
    }

    public void validateKoreatechEmail() {
        if (!domainForm().equals(KOREATECH_DOMAIN)) {
            throw new GlobalException(ExceptionMessage.INVALID_EMAIL_ADDRESS);
        }
    }

    private String domainForm() {
        return email.substring(getSeparateIndex() + DOMAIN_SEPARATOR.length());
    }

    private int getSeparateIndex() {
        return email.lastIndexOf(DOMAIN_SEPARATOR);
    }

}
