package kr.ac.koreatech.sw.kosp.domain.mail.service;

import kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event.EmailVerificationSendEvent;
import kr.ac.koreatech.sw.kosp.domain.mail.model.EmailVerification;
import kr.ac.koreatech.sw.kosp.domain.mail.repository.EmailVerificationRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationRepository emailVerificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    private static final long CODE_TTL = 300L; // 5 minutes

    private static final long SIGNUP_WINDOW_TTL = 1800L; // 30 minutes for signup completion

    @Transactional
    public void sendCertificationMail(String email) {
        String code = generateCode();
        
        // 1. Save to Redis
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .isVerified(false)
                .ttl(CODE_TTL)
                .build();
        
        emailVerificationRepository.save(verification);

        // 2. Publish Event
        eventPublisher.publishEvent(new EmailVerificationSendEvent(email, code));
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findById(email)
                .orElseThrow(() -> new GlobalException(EMAIL_NOT_FOUND));

        if (!verification.getCode().equals(code)) {
            throw new GlobalException(INVALID_VERIFICATION_CODE);
        }

        verification.verify();
        verification.updateTtl(SIGNUP_WINDOW_TTL); // Extend TTL for signup
        emailVerificationRepository.save(verification);
        return true;
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
