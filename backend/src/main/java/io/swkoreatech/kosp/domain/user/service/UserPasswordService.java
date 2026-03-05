package io.swkoreatech.kosp.domain.user.service;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.user.model.PasswordResetToken;
import io.swkoreatech.kosp.domain.user.repository.PasswordResetTokenRepository;
import io.swkoreatech.kosp.infra.email.eventlistener.event.ResetPasswordEvent;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 비밀번호 서비스.
 * 비밀번호 재설정 메일 발송 및 비밀번호 재설정 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
public class UserPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 재설정 메일을 발송한다.
     * Redis에 재설정 토큰을 저장하고, 이메일 발송 이벤트를 발행한다.
     *
     * @param email 사용자 이메일
     * @param clientUrl 클라이언트 비밀번호 재설정 URL
     * @throws GlobalException 이메일이 존재하지 않는 경우
     */
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

    /**
     * 토큰을 검증하고 비밀번호를 재설정한다.
     * 재설정 완료 후 사용된 토큰을 삭제한다.
     *
     * @param token 비밀번호 재설정 토큰
     * @param newPassword 새 비밀번호
     * @throws GlobalException 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.getById(token);

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));

        user.changePassword(newPassword, passwordEncoder);
        
        passwordResetTokenRepository.delete(resetToken);
    }
}
