package io.swkoreatech.kosp.infra.email.client;

import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/**
 * AWS SES 이메일 발송 클라이언트.
 *
 * <p>Spring Retry를 통해 발송 실패 시 자동 재시도하며,
 * 최종 실패 시 {@link #mailRecovery}에서 에러를 로깅한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SesMailSender {

    private final SesClient sesClient;

    /**
     * SES를 통해 이메일을 발송한다. 실패 시 자동 재시도된다.
     *
     * @param request AWS SES 이메일 발송 요청 객체
     */
    @Retryable
    public void sendMail(SendEmailRequest request) {
        sesClient.sendEmail(request);
    }

    /**
     * 이메일 발송 최종 실패 시 호출되는 복구 메서드.
     *
     * @param e       발생한 예외
     * @param from    발신자 이메일 주소
     * @param to      수신자 이메일 주소
     * @param subject 이메일 제목
     */
    @Recover
    public void mailRecovery(Exception e, String from, String to, String subject) {
        log.error("메일 전송에 실패했습니다. from: {}, to: {}, subject: {}", from, to, subject, e);
    }
}
