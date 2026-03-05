package io.swkoreatech.kosp.infra.email.model;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;

import jakarta.validation.constraints.Email;

/**
 * 이메일 주소 값 객체.
 *
 * <p>이메일 형식 검증과 한국기술교육대학교(koreatech.ac.kr) 도메인 검증을 제공한다.
 *
 * @param email 이메일 주소 문자열
 */
public record EmailAddress(
    @Email(message = "이메일 형식을 지켜주세요.", regexp = EmailAddress.EMAIL_PATTERN)
    String email
) {
    private static final String LOCAL_PARTS_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@";
    private static final String DOMAIN_PATTERN = "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
    private static final String EMAIL_PATTERN = LOCAL_PARTS_PATTERN + DOMAIN_PATTERN;

    private static final String DOMAIN_SEPARATOR = "@";
    private static final String KOREATECH_DOMAIN = "koreatech.ac.kr";

    /**
     * 문자열로부터 {@link EmailAddress}를 생성한다.
     *
     * @param email 이메일 주소 문자열
     * @return 생성된 {@link EmailAddress} 인스턴스
     */
    public static EmailAddress from(String email) {
        return new EmailAddress(email);
    }

    /**
     * 한국기술교육대학교(koreatech.ac.kr) 이메일인지 검증한다.
     *
     * @throws GlobalException 도메인이 koreatech.ac.kr이 아닌 경우
     */
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
