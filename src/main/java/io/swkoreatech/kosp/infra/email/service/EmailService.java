package io.swkoreatech.kosp.infra.email.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import io.swkoreatech.kosp.infra.email.client.SesMailSender;
import io.swkoreatech.kosp.infra.email.form.EmailForm;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String NO_REPLY_EMAIL_ADDRESS = "no-reply@swkoreatech.io";

    private final SesMailSender sesMailSender;
    private final TemplateEngine templateEngine;

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
