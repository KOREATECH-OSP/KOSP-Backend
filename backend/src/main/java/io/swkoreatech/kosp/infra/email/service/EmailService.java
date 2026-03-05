package io.swkoreatech.kosp.infra.email.service;

import io.swkoreatech.kosp.infra.email.client.SesMailSender;
import io.swkoreatech.kosp.infra.email.form.EmailForm;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/**
 * 이메일 발송 서비스.
 *
 * <p>Thymeleaf 템플릿 엔진으로 HTML 본문을 렌더링하고 AWS SES를 통해 이메일을 발송한다.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String NO_REPLY_EMAIL_ADDRESS = "no-reply@swkoreatech.io";

    private final SesMailSender sesMailSender;
    private final TemplateEngine templateEngine;

    /**
     * 지정된 수신자에게 이메일을 발송한다.
     *
     * @param targetEmail 수신자 이메일 주소
     * @param emailForm   이메일 양식 (제목, 콘텐츠, 템플릿 경로)
     */
    public void sendEmail(String targetEmail, EmailForm emailForm) {
        SendEmailRequest request = createEmailRequest(targetEmail, emailForm);
        sesMailSender.sendMail(request);
    }

    private SendEmailRequest createEmailRequest(String targetEmail, EmailForm emailForm) {
        Context context = new Context();
        Map<String, String> contents = emailForm.getContent();
        contents.forEach(context::setVariable);

        String htmlBody = templateEngine.process(emailForm.getFilePath(), context);

        return SendEmailRequest.builder()
            .destination(destination -> destination.toAddresses(targetEmail))
            .source(NO_REPLY_EMAIL_ADDRESS)
            .message(message -> message
                .subject(content -> content
                    .charset("UTF-8")
                    .data(emailForm.getSubject())
                )
                .body(body -> body
                    .html(content -> content
                        .charset("UTF-8")
                        .data(htmlBody)
                    )
                )
            )
            .build();
    }
}
