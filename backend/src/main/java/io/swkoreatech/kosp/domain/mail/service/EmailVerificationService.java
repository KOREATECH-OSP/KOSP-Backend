package io.swkoreatech.kosp.domain.mail.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.mail.model.EmailVerification;
import io.swkoreatech.kosp.domain.mail.repository.EmailVerificationRepository;
import io.swkoreatech.kosp.global.auth.token.TokenType;
import io.swkoreatech.kosp.infra.email.eventlistener.event.EmailVerificationSendEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 이메일 인증 서비스.
 * 인증 코드 발송, 코드 검증, 회원가입 인증 완료 기능을 담당한다.
 */
@Slf4j
@Service
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final long CODE_TTL = 300L; // 5 minutes (seconds)

    public EmailVerificationService(
        EmailVerificationRepository emailVerificationRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 인증 코드를 생성하여 이메일로 발송한다.
     *
     * @param email 이메일 주소
     * @param signupToken 회원가입 토큰
     */
    @Transactional
    public void sendCertificationMail(String email, String signupToken) {
        String code = generateCode();

        // 1. Save to Redis
        EmailVerification verification = EmailVerification.builder()
            .email(email)
            .code(code)
            .signupToken(signupToken)
            .isVerified(false)
            .ttl(CODE_TTL)
            .build();

        emailVerificationRepository.save(verification);

        // 2. Publish Event
        eventPublisher.publishEvent(new EmailVerificationSendEvent(email, code));
    }

    /**
     * 인증 코드를 검증한다.
     *
     * @param email 이메일 주소
     * @param code 인증 코드
     * @return 검증된 이메일 인증 정보
     * @throws GlobalException 인증 코드가 일치하지 않는 경우
     */
    @Transactional
    public EmailVerification verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.getById(email);

        if (!verification.getCode().equals(code)) {
            throw new GlobalException(ExceptionMessage.INVALID_VERIFICATION_CODE);
        }

        verification.verify();
        verification.updateTtl(TimeUnit.MILLISECONDS.toSeconds(TokenType.SIGNUP.getExpiration()));
        emailVerificationRepository.save(verification);
        return verification;
    }

    /**
     * 회원가입 인증을 완료 처리한다.
     *
     * @param email 이메일 주소
     * @throws GlobalException 이메일이 인증되지 않은 경우
     */
    @Transactional
    public void completeSignupVerification(String email) {
        EmailVerification verification = emailVerificationRepository.getById(email);

        if (!verification.isVerified()) {
            throw new GlobalException(ExceptionMessage.EMAIL_NOT_VERIFIED);
        }

        emailVerificationRepository.delete(verification);
    }

    /**
     * 이메일 인증 정보를 조회한다.
     *
     * @param email 이메일 주소
     * @return 이메일 인증 정보
     */
    public EmailVerification getVerification(String email) {
        return emailVerificationRepository.getById(email);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
