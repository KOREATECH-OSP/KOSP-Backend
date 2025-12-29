package kr.ac.koreatech.sw.kosp.domain.user.service;

import kr.ac.koreatech.sw.kosp.domain.user.model.PasswordResetToken;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.PasswordResetTokenRepository;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event.ResetPasswordEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Value("${server.url:http://localhost:3000}") // Default to frontend URL
    private String serverUrl;

    @Transactional
    public void sendPasswordResetMail(String email) {
        User user = userRepository.findByKutEmail(email)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.EMAIL_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .ttl(600L) // 10 minutes
                .build();
        
        passwordResetTokenRepository.save(resetToken);

        eventPublisher.publishEvent(new ResetPasswordEvent(email, serverUrl, token));
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findById(token)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.INVALID_VERIFICATION_CODE));

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));

        user.changePassword(newPassword, passwordEncoder);
        
        passwordResetTokenRepository.delete(resetToken);
    }
}
