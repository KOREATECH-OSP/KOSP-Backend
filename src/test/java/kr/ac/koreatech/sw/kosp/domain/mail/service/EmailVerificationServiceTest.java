package kr.ac.koreatech.sw.kosp.domain.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import kr.ac.koreatech.sw.kosp.domain.mail.model.EmailVerification;
import kr.ac.koreatech.sw.kosp.domain.mail.repository.EmailVerificationRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event.EmailVerificationSendEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationService 단위 테스트")
class EmailVerificationServiceTest {

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EmailVerification createVerification(String email, String code, boolean isVerified) {
        return EmailVerification.builder()
            .email(email)
            .code(code)
            .signupToken("signup-token")
            .isVerified(isVerified)
            .ttl(300L)
            .build();
    }

    @Nested
    @DisplayName("sendCertificationMail 메서드")
    class SendCertificationMailTest {

        @Test
        @DisplayName("인증 메일을 발송하고 Redis에 저장한다")
        void sendsCertificationMail_andSavesToRedis() {
            // given
            String email = "test@koreatech.ac.kr";
            String signupToken = "signup-token";

            // when
            emailVerificationService.sendCertificationMail(email, signupToken);

            // then
            ArgumentCaptor<EmailVerification> verificationCaptor = ArgumentCaptor.forClass(EmailVerification.class);
            verify(emailVerificationRepository).save(verificationCaptor.capture());
            
            EmailVerification captured = verificationCaptor.getValue();
            assertThat(captured.getEmail()).isEqualTo(email);
            assertThat(captured.getCode()).hasSize(6);
            assertThat(captured.getSignupToken()).isEqualTo(signupToken);
            assertThat(captured.isVerified()).isFalse();

            ArgumentCaptor<EmailVerificationSendEvent> eventCaptor = ArgumentCaptor.forClass(EmailVerificationSendEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().email()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("verifyCode 메서드")
    class VerifyCodeTest {

        @Test
        @DisplayName("이메일이 존재하지 않으면 예외가 발생한다")
        void throwsException_whenEmailNotFound() {
            // given
            String email = "test@koreatech.ac.kr";
            given(emailVerificationRepository.findById(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> emailVerificationService.verifyCode(email, "123456"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("인증 코드가 일치하지 않으면 예외가 발생한다")
        void throwsException_whenCodeDoesNotMatch() {
            // given
            String email = "test@koreatech.ac.kr";
            EmailVerification verification = createVerification(email, "123456", false);
            given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> emailVerificationService.verifyCode(email, "654321"))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("인증 코드가 일치하면 인증 완료 처리한다")
        void verifiesEmail_whenCodeMatches() {
            // given
            String email = "test@koreatech.ac.kr";
            String code = "123456";
            EmailVerification verification = createVerification(email, code, false);
            given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

            // when
            EmailVerification result = emailVerificationService.verifyCode(email, code);

            // then
            assertThat(result.isVerified()).isTrue();
            verify(emailVerificationRepository).save(verification);
        }
    }

    @Nested
    @DisplayName("completeSignupVerification 메서드")
    class CompleteSignupVerificationTest {

        @Test
        @DisplayName("이메일이 존재하지 않으면 예외가 발생한다")
        void throwsException_whenEmailNotFound() {
            // given
            String email = "test@koreatech.ac.kr";
            given(emailVerificationRepository.findById(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> emailVerificationService.completeSignupVerification(email))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("이메일이 인증되지 않았으면 예외가 발생한다")
        void throwsException_whenEmailNotVerified() {
            // given
            String email = "test@koreatech.ac.kr";
            EmailVerification verification = createVerification(email, "123456", false);
            given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> emailVerificationService.completeSignupVerification(email))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("인증된 이메일이면 Redis에서 삭제한다")
        void deletesVerification_whenEmailVerified() {
            // given
            String email = "test@koreatech.ac.kr";
            EmailVerification verification = createVerification(email, "123456", true);
            given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

            // when
            emailVerificationService.completeSignupVerification(email);

            // then
            verify(emailVerificationRepository).delete(verification);
        }
    }

    @Nested
    @DisplayName("getVerification 메서드")
    class GetVerificationTest {

        @Test
        @DisplayName("이메일이 존재하지 않으면 예외가 발생한다")
        void throwsException_whenEmailNotFound() {
            // given
            String email = "test@koreatech.ac.kr";
            given(emailVerificationRepository.findById(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> emailVerificationService.getVerification(email))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("인증 정보를 정상적으로 반환한다")
        void returnsVerification() {
            // given
            String email = "test@koreatech.ac.kr";
            EmailVerification verification = createVerification(email, "123456", false);
            given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

            // when
            EmailVerification result = emailVerificationService.getVerification(email);

            // then
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getCode()).isEqualTo("123456");
        }
    }
}
