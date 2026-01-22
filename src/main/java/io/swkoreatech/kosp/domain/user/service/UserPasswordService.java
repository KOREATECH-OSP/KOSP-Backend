package io.swkoreatech.kosp.domain.user.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.user.model.PasswordResetToken;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.PasswordResetTokenRepository;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import io.swkoreatech.kosp.infra.email.eventlistener.event.ResetPasswordEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendPasswordResetMail(String email, String clientUrl) {
        User user = userRepository.findByKutEmail(email)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.EMAIL_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .ttl(600L) // 10 minutes
                .build();
        
        passwordResetTokenRepository.save(resetToken);

        eventPublisher.publishEvent(new ResetPasswordEvent(email, clientUrl, token));
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
