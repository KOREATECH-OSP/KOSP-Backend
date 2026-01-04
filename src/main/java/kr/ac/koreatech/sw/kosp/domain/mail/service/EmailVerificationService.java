package kr.ac.koreatech.sw.kosp.domain.mail.service;

import kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event.EmailVerificationSendEvent;
import kr.ac.koreatech.sw.kosp.domain.mail.model.EmailVerification;
import kr.ac.koreatech.sw.kosp.domain.mail.repository.EmailVerificationRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.*;

@Slf4j
@Service
public class EmailVerificationService {
    private final EmailVerificationRepository emailVerificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final long signupTokenTtl;
    
    private static final long CODE_TTL = 300L; // 5 minutes (seconds)

    public EmailVerificationService(
        EmailVerificationRepository emailVerificationRepository,
        ApplicationEventPublisher eventPublisher,
        @Value("${jwt.expiration-time.signup-token}") long signupTokenTtl
    ) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.eventPublisher = eventPublisher;
        this.signupTokenTtl = signupTokenTtl;
    }

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

    @Transactional
    public EmailVerification verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findById(email)
                .orElseThrow(() -> new GlobalException(EMAIL_NOT_FOUND));

        if (!verification.getCode().equals(code)) {
            throw new GlobalException(INVALID_VERIFICATION_CODE);
        }

        verification.verify();
        verification.updateTtl(TimeUnit.MILLISECONDS.toSeconds(signupTokenTtl));
        emailVerificationRepository.save(verification);
        return verification;
    }
    
    @Transactional
    public void completeSignupVerification(String email) {
        EmailVerification verification = emailVerificationRepository.findById(email)
                .orElseThrow(() -> new GlobalException(EMAIL_NOT_FOUND));

        if (!verification.isVerified()) {
            throw new GlobalException(EMAIL_NOT_VERIFIED);
        }

        emailVerificationRepository.delete(verification);
    }
    
    public EmailVerification getVerification(String email) {
        return emailVerificationRepository.findById(email)
                .orElseThrow(() -> new GlobalException(EMAIL_NOT_FOUND));
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
